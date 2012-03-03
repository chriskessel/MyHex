package kessel.hex.orders.agent;

import kessel.hex.domain.Agent;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.AbstractOrderTest;
import kessel.hex.orders.wizard.CharmFigure;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test training agents. */
public class TrainAgentTest extends AbstractOrderTest<TrainAgent>
{
  private static final Logger LOG = Logger.getLogger( TrainAgentTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Agent agent = playerA.getAgents().get( 0 );
    TrainAgent oldOrder = new TrainAgent( agent );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    TrainAgent newOrder = Game.GSON.fromJson( jsonOrder, TrainAgent.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testCharmed()
  {
    // Execute the charm.
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardForB = playerB.getWizards().get( 0 );
    wizardForB.setLevel( 2 );
    Agent agentForA = playerA.getAgents().get( 0 );

    CharmFigure charmOrder = new CharmFigure( wizardForB, agentForA );
    charmOrder.execute( game );

    assertTrue( charmOrder.wasExecuted() );
    assertEquals( 1, game.getCurrentTurn().getOrdersOfType( CharmFigure.class ).size() );

    // Now try the train, it should fail due to the charm.
    playerA.setGold( 10000 );
    TrainAgent order = new TrainAgent( agentForA );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testNotEnoughMoney()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Agent agent = playerA.getAgents().get( 0 );
    TrainAgent order = new TrainAgent( agent );
    playerA.setGold( 0 );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testLevelCap()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Agent agent = playerA.getAgents().get( 0 );
    playerA.setGold( 10000 );
    agent.setLevel( 20 );
    TrainAgent order = new TrainAgent( agent );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testTrained()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    playerA.setGold( 10000 );
    Agent agent = playerA.getAgents().get( 0 );
    int oldLevel = agent.getLevel();
    TrainAgent order = new TrainAgent( agent );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( oldLevel + 1, agent.getLevel() );
  }
}
