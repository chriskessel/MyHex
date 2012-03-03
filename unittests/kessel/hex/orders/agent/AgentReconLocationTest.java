package kessel.hex.orders.agent;

import kessel.hex.domain.Agent;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.AbstractOrderTest;
import kessel.hex.orders.wizard.CharmFigure;
import kessel.hex.util.Tuple;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test agent recon. */
public class AgentReconLocationTest extends AbstractOrderTest<AgentReconLocation>
{
  private static final Logger LOG = Logger.getLogger( AgentReconLocationTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( AgentReconLocation a, AgentReconLocation b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a._target, b._target );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Agent agent = playerA.getAgents().get( 0 );
    AgentReconLocation oldOrder = new AgentReconLocation( agent, new Tuple( 1, 0 ) );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );

    AgentReconLocation newOrder = Game.GSON.fromJson( jsonOrder, AgentReconLocation.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testTargetOffMap()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Agent agent = playerA.getAgents().get( 0 );
    AgentReconLocation order = new AgentReconLocation( agent, new Tuple( 3, 3 ) );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testTargetTooFar()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Agent agent = playerA.getAgents().get( 0 );
    agent.setBase( game.getPlayers().get( 0 ).getCapitol() );
    agent.setRange( 0 );
    AgentReconLocation order = new AgentReconLocation( agent, new Tuple( 1, 1 ) );
    order.execute( game );
    assertFalse( order.wasExecuted() );
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

    // Now try the recon, it should fail due to the charm.
    agentForA.setBase( game.getPlayers().get( 0 ).getCapitol() );
    AgentReconLocation reconOrder = new AgentReconLocation( agentForA, new Tuple( 1, 0 ) );

    assertEquals( 2, playerA.getKnownRegions().size() );
    assertEquals( 24, playerA.getKnownItems().size() );
    reconOrder.execute( game );
    assertFalse( reconOrder.wasExecuted() );
  }

  @Test
  public void testStandardRecon()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Agent agent = playerA.getAgents().get( 0 );
    agent.setBase( game.getPlayers().get( 0 ).getCapitol() );
    AgentReconLocation order = new AgentReconLocation( agent, new Tuple( 1, 0 ) );

    assertEquals( 2, playerA.getKnownRegions().size() );
    assertEquals( 24, playerA.getKnownItems().size() );
    assertEquals( 2, playerA.getKnownRegions().get( 0 ).getLocations().size() );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 2, playerA.getKnownRegions().size() );
    assertEquals( 2, playerA.getKnownRegions().get( 0 ).getLocations().size() );
    assertEquals( 44, playerA.getKnownItems().size() );
    assertNotNull( playerA.getAgent( agent.getId() ) );
  }

  @Test
  public void testAgentDeath()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Agent agent = playerA.getAgents().get( 0 );
    agent.setBase( game.getPlayers().get( 0 ).getCapitol() );
    AgentReconLocation order = new AgentReconLocation( agent, new Tuple( 1, 0 ) )
    {
      protected boolean checkForAgentDeath( Game game ) { return true; }
    };

    assertEquals( 2, playerA.getKnownRegions().size() );
    assertEquals( 24, playerA.getKnownItems().size() );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 2, playerA.getKnownRegions().size() );
    assertEquals( 2, playerA.getKnownRegions().get( 0 ).getLocations().size() );
    assertEquals( 43, playerA.getKnownItems().size() );
    assertNull( playerA.getAgent( agent.getId() ) );
  }
}
