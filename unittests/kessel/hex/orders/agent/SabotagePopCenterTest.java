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
public class SabotagePopCenterTest extends AbstractOrderTest<SabotagePopCenter>
{
  private static final Logger LOG = Logger.getLogger( SabotagePopCenterTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( SabotagePopCenter a, SabotagePopCenter b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a.getTarget().getId(), b._jsonTargetId.intValue() );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Agent agent = playerA.getAgents().get( 0 );
    PopCenter city = game.getPopCenter( 2, 2 );
    SabotagePopCenter oldOrder = new SabotagePopCenter( agent, city );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    SabotagePopCenter newOrder = Game.GSON.fromJson( jsonOrder, SabotagePopCenter.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testTargetMissing()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Agent agent = playerA.getAgents().get( 0 );
    PopCenter fakePop = new PopCenter( game.generateUniqueId(), "foo", 0, agent.getLocation(), PopCenter.PopType.City );
    fakePop.setLevel( 2 );
    SabotagePopCenter order = new SabotagePopCenter( agent, fakePop );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), SabotagePopCenter.class );
    order.fixDeserializationReferences( game );

    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testTargetOutOfRange()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Agent agent = playerA.getAgents().get( 0 );
    PopCenter pop = game.getPopCenter( 2, 2 );
    agent.setRange( 0 );
    SabotagePopCenter order = new SabotagePopCenter( agent, pop );

    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testTargetOutOfMoney()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Agent agent = playerA.getAgents().get( 0 );
    PopCenter pop = game.getPopCenter( 2, 2 );
    playerA.setGold( 0 );
    SabotagePopCenter order = new SabotagePopCenter( agent, pop );

    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testSuccessNotKilled()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Agent agent = playerA.getAgents().get( 0 );
    PopCenter pop = game.getPopCenter( 2, 2 );
    pop.setLevel( 2 );
    playerA.setGold( 10000 );
    SabotagePopCenter order = new SabotagePopCenter( agent, pop )
    {
      public boolean makeAttempt( Game game ) { return true; }

      protected boolean checkForAgentDeath( Game game ) { return false; }
    };

    assertEquals( 4, agent.getLevel() );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 1, pop.getLevel() );
    assertNotNull( playerA.getAgent( agent.getId() ) );
    assertEquals( 5, agent.getLevel() );
  }

  @Test
  public void testFailureKilled()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Agent agent = playerA.getAgents().get( 0 );
    PopCenter pop = game.getPopCenter( 2, 2 );
    pop.setLevel( 2 );
    playerB.improveEmbassy( pop.getLocation().getRegion() );
    playerA.setGold( 10000 );
    SabotagePopCenter order = new SabotagePopCenter( agent, pop )
    {
      public boolean makeAttempt( Game game ) { return false; }

      protected boolean checkForAgentDeath( Game game ) { return true; }
    };

    assertEquals( 1, playerB.getEmbassyLevel( pop.getLocation().getRegion() ) );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertNull( playerA.getAgent( agent.getId() ) );
    assertEquals( 2, pop.getLevel() );
    assertEquals( 1, playerB.getEmbassyLevel( pop.getLocation().getRegion() ) );
  }
}
