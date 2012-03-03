package kessel.hex.orders.agent;

import kessel.hex.domain.Agent;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.orders.AbstractOrderTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test embassy sabotage */
public class SabotageEmbassyTest extends AbstractOrderTest<SabotageEmbassy>
{
  private static final Logger LOG = Logger.getLogger( SabotageEmbassyTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( SabotageEmbassy a, SabotageEmbassy b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a.getRegionalCity().getId(), b._jsonCityId.intValue() );
    assertEquals( a._playerName, b._playerName );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Agent agent = playerA.getAgents().get( 0 );
    PopCenter city = game.getPopCenter( 2, 2 );
    SabotageEmbassy oldOrder = new SabotageEmbassy( agent, city, playerB.getName() );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    SabotageEmbassy newOrder = Game.GSON.fromJson( jsonOrder, SabotageEmbassy.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testTargetNotCity()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Agent agent = playerA.getAgents().get( 0 );
    PopCenter pop = game.getPopCenter( 1, 1 );
    SabotageEmbassy order = new SabotageEmbassy( agent, pop, playerB.getName() );

    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testTargetOutOfRange()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Agent agent = playerA.getAgents().get( 0 );
    PopCenter pop = game.getPopCenter( 2, 2 );
    agent.setRange( 0 );
    SabotageEmbassy order = new SabotageEmbassy( agent, pop, playerB.getName() );

    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testTargetOutOfMoney()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Agent agent = playerA.getAgents().get( 0 );
    PopCenter pop = game.getPopCenter( 2, 2 );
    playerA.setGold( 0 );
    SabotageEmbassy order = new SabotageEmbassy( agent, pop, playerB.getName() );

    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testSuccessNotKilled()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Agent agent = playerA.getAgents().get( 0 );
    PopCenter pop = game.getPopCenter( 2, 2 );
    playerB.improveEmbassy( pop.getLocation().getRegion() );
    playerA.setGold( 10000 );
    SabotageEmbassy order = new SabotageEmbassy( agent, pop, playerB.getName() )
    {
      public boolean makeAttempt( Game game ) { return true; }

      protected boolean checkForAgentDeath( Game game ) { return false; }
    };

    assertEquals( 1, playerB.getEmbassyLevel( pop.getLocation().getRegion() ) );
    assertEquals( 4, agent.getLevel() );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertNotNull( playerA.getAgent( agent.getId() ) );
    assertEquals( 5, agent.getLevel() );
    assertEquals( 0, playerB.getEmbassyLevel( pop.getLocation().getRegion() ) );
  }

  @Test
  public void testFailureKilled()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Agent agent = playerA.getAgents().get( 0 );
    PopCenter pop = game.getPopCenter( 2, 2 );
    playerB.improveEmbassy( pop.getLocation().getRegion() );
    playerA.setGold( 10000 );
    SabotageEmbassy order = new SabotageEmbassy( agent, pop, playerB.getName() )
    {
      public boolean makeAttempt( Game game ) { return false; }

      protected boolean checkForAgentDeath( Game game ) { return true; }
    };

    assertEquals( 1, playerB.getEmbassyLevel( pop.getLocation().getRegion() ) );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertNull( playerA.getAgent( agent.getId() ) );
    assertEquals( 1, playerB.getEmbassyLevel( pop.getLocation().getRegion() ) );
  }
}
