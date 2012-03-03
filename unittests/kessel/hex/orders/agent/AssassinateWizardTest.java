package kessel.hex.orders.agent;

import kessel.hex.domain.Agent;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.Wizard;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test wizard assassination. */
public class AssassinateWizardTest extends AbstractAssassinateFigureTest<AssassinateWizard>
{
  private static final Logger LOG = Logger.getLogger( AssassinateWizardTest.class );

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
    Wizard wizForB = playerB.getWizards().get( 0 );
    AssassinateWizard oldOrder = new AssassinateWizard( agent, wizForB, wizForB.getBase() );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    AssassinateWizard newOrder = Game.GSON.fromJson( jsonOrder, AssassinateWizard.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testTargetMissing()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Agent agent = playerA.getAgents().get( 0 );
    Wizard wizForB = playerB.getWizards().get( 0 );
    playerB.remove( wizForB );
    AssassinateWizard order = new AssassinateWizard( agent, wizForB, wizForB.getBase() );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), AssassinateWizard.class );
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
    Wizard wizForB = playerB.getWizards().get( 0 );
    AssassinateWizard order = new AssassinateWizard( agent, wizForB, playerA.getCapitol() );

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
    Wizard wizForB = playerB.getWizards().get( 0 );
    AssassinateWizard order = new AssassinateWizard( agent, wizForB, wizForB.getBase() );

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
    Wizard wizForB = playerB.getWizards().get( 0 );
    playerA.setGold( 0 );
    AssassinateWizard order = new AssassinateWizard( agent, wizForB, wizForB.getBase() );

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
    Wizard wizForB = playerB.getWizards().get( 0 );
    AssassinateWizard order = new AssassinateWizard( agent, wizForB, wizForB.getBase() )
    {
      public boolean makeAttempt( Game game ) { return true; }

      protected boolean checkForAgentDeath( Game game ) { return false; }
    };

    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertNotNull( playerA.getAgent( agent.getId() ) );
    assertNull( playerB.getWizard( wizForB.getId() ) );
  }

  @Test
  public void testFailureKilled()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Agent agent = playerA.getAgents().get( 0 );
    Wizard wizForB = playerB.getWizards().get( 0 );
    AssassinateWizard order = new AssassinateWizard( agent, wizForB, wizForB.getBase() )
    {
      public boolean makeAttempt( Game game ) { return false; }

      protected boolean checkForAgentDeath( Game game ) { return true; }
    };

    order.execute( game );
    assertNull( playerA.getAgent( agent.getId() ) );
    assertNotNull( playerB.getWizard( wizForB.getId() ) );
  }
}
