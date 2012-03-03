package kessel.hex.orders.agent;

import kessel.hex.domain.Agent;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test Agent assassination. */
public class AssassinateAgentTest extends AbstractAssassinateFigureTest<AssassinateAgent>
{
  private static final Logger LOG = Logger.getLogger( AssassinateAgentTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Agent agent = playerA.getAgents().get( 0 );
    Agent agentForB = playerB.getAgents().get( 0 );
    AssassinateAgent oldOrder = new AssassinateAgent( agent, agentForB, agentForB.getBase() );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    AssassinateAgent newOrder = Game.GSON.fromJson( jsonOrder, AssassinateAgent.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testTargetMissing()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Agent agent = playerA.getAgents().get( 0 );
    Agent agentForB = playerB.getAgents().get( 0 );
    playerB.remove( agentForB );
    AssassinateAgent order = new AssassinateAgent( agent, agentForB, agentForB.getBase() );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), AssassinateAgent.class );
    order.fixDeserializationReferences( game );

    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testTargetNotAtBase()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Agent agent = playerA.getAgents().get( 0 );
    Agent agentForB = playerB.getAgents().get( 0 );
    AssassinateAgent order = new AssassinateAgent( agent, agentForB, playerA.getCapitol() );

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
    agent.setRange( 0 );
    Agent agentForB = playerB.getAgents().get( 0 );
    AssassinateAgent order = new AssassinateAgent( agent, agentForB, agentForB.getBase() );

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
    Agent agentForB = playerB.getAgents().get( 0 );
    playerA.setGold( 0 );
    AssassinateAgent order = new AssassinateAgent( agent, agentForB, agentForB.getBase() );

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
    Agent agentForB = playerB.getAgents().get( 0 );
    AssassinateAgent order = new AssassinateAgent( agent, agentForB, agentForB.getBase() )
    {
      public boolean makeAttempt( Game game ) { return true; }

      protected boolean checkForAgentDeath( Game game ) { return false; }
    };

    assertEquals( 4, agent.getLevel() );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertNotNull( playerA.getAgent( agent.getId() ) );
    assertNull( playerB.getAgent( agentForB.getId() ) );
    assertEquals( 5, agent.getLevel() );
  }

  @Test
  public void testFailureKilledCounterWorked()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Agent agent = playerA.getAgents().get( 0 );
    Agent agentForB = playerB.getAgents().get( 0 );
    AssassinateAgent order = new AssassinateAgent( agent, agentForB, agentForB.getBase() )
    {
      public boolean makeAttempt( Game game ) { return false; }

      protected boolean checkForAgentDeath( Game game ) { return true; }

      protected int determineResistance( Game game ) { return 1; }
    };

    // Set levels to ensure counter espionage is seen as successful.
    agent.setLevel( 1 );
    playerB.setGold( 100000 );
    agentForB.setLevel( 12 );
    CounterEspionage counterEspionageOrder = new CounterEspionage( agentForB );
    counterEspionageOrder.execute( game );
    assertTrue( counterEspionageOrder.wasExecuted() );

    order.execute( game );
    assertNull( playerA.getAgent( agent.getId() ) );
    assertNotNull( playerB.getAgent( agentForB.getId() ) );
    assertEquals( 13, agentForB.getLevel() );
  }
}
