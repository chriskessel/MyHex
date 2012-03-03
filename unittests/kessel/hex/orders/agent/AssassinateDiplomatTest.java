package kessel.hex.orders.agent;

import kessel.hex.domain.Agent;
import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.wizard.CharmFigure;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test diplomat assassination. */
public class AssassinateDiplomatTest extends AbstractAssassinateFigureTest<AssassinateDiplomat>
{
  private static final Logger LOG = Logger.getLogger( AssassinateDiplomatTest.class );

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
    Diplomat diplomatForB = playerB.getDiplomats().get( 0 );
    AssassinateDiplomat oldOrder = new AssassinateDiplomat( agent, diplomatForB, diplomatForB.getBase() );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    AssassinateDiplomat newOrder = Game.GSON.fromJson( jsonOrder, AssassinateDiplomat.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testTargetMissing()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Agent agent = playerA.getAgents().get( 0 );
    Diplomat diplomatForB = playerB.getDiplomats().get( 0 );
    playerB.remove( diplomatForB );
    AssassinateDiplomat order = new AssassinateDiplomat( agent, diplomatForB, diplomatForB.getBase() );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), AssassinateDiplomat.class );
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
    Diplomat diplomatForB = playerB.getDiplomats().get( 0 );
    AssassinateDiplomat order = new AssassinateDiplomat( agent, diplomatForB, playerA.getCapitol() );

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
    Diplomat diplomatForB = playerB.getDiplomats().get( 0 );
    AssassinateDiplomat order = new AssassinateDiplomat( agent, diplomatForB, diplomatForB.getBase() );

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
    Diplomat diplomatForB = playerB.getDiplomats().get( 0 );
    playerA.setGold( 0 );
    AssassinateDiplomat order = new AssassinateDiplomat( agent, diplomatForB, diplomatForB.getBase() );

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
    Diplomat diplomatForB = playerB.getDiplomats().get( 0 );
    AssassinateDiplomat order = new AssassinateDiplomat( agent, diplomatForB, diplomatForB.getBase() )
    {
      public boolean makeAttempt( Game game ) { return true; }

      protected boolean checkForAgentDeath( Game game ) { return false; }
    };

    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertNotNull( playerA.getAgent( agent.getId() ) );
    assertNull( playerB.getDiplomat( diplomatForB.getId() ) );
  }

  @Test
  public void testFailureKilled()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Agent agent = playerA.getAgents().get( 0 );
    Diplomat diplomatForB = playerB.getDiplomats().get( 0 );
    AssassinateDiplomat order = new AssassinateDiplomat( agent, diplomatForB, diplomatForB.getBase() )
    {
      public boolean makeAttempt( Game game ) { return false; }

      protected boolean checkForAgentDeath( Game game ) { return true; }
    };

    order.execute( game );
    assertNull( playerA.getAgent( agent.getId() ) );
    assertNotNull( playerB.getDiplomat( diplomatForB.getId() ) );
  }

  @Test
  public void testCharmImpact()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Agent agent = playerA.getAgents().get( 0 );
    Diplomat diplomatForB = playerB.getDiplomats().get( 0 );
    AssassinateDiplomat assassinationOrder = new AssassinateDiplomat( agent, diplomatForB, diplomatForB.getBase() );
    assertEquals( 19, assassinationOrder.determineResistance( game ) );

    // Cast the charm, the resistance should drop.
    Wizard wizForA = playerA.getWizards().get( 0 );
    wizForA.setLevel( 2 );
    CharmFigure charmOrder = new CharmFigure( wizForA, diplomatForB );
    charmOrder.execute( game );
    assertTrue( charmOrder.wasExecuted() );

    assertEquals( 9, assassinationOrder.determineResistance( game ) );
  }
}
