package kessel.hex.orders.wizard;

import kessel.hex.domain.Army;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.army.AbstractArmyAttack;
import kessel.hex.orders.army.ArmyAttackArmy;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

public class DeepFogTest extends AbstractCombatSpellTest<DeepFog>
{
  private static final Logger LOG = Logger.getLogger( DeepFogTest.class );

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
    Wizard wizardForA = playerA.getWizards().get( 0 );
    Army armyForB = playerB.getActiveArmies().get( 0 );
    DeepFog oldOrder = new DeepFog( wizardForA, armyForB );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    DeepFog newOrder = Game.GSON.fromJson( jsonOrder, DeepFog.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testNotInArmy()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setBase( playerA.getPopCenters().get( 0 ) );
    Army armyForB = playerB.getArmies().get( 0 );

    // Cast the deep fog spell.
    DeepFog order = new DeepFog( wizardForA, armyForB );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testWorksIfAttacker()
  {
    playerAttackPlayer( true );
  }

  @Test
  public void testWorksIfDefender()
  {
    playerAttackPlayer( false );
  }

  private void playerAttackPlayer( boolean aIsAggressor )
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    Army armyForB = playerB.getArmies().get( 0 );

    // Cast the deep fog spell.
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setBase( armyForA );
    armyForA.setLocation( armyForB.getLocation() );
    wizardForA.setLevel( 3 );
    DeepFog order = new DeepFog( wizardForA, armyForB );
    order.execute( game );
    assertTrue( order.wasExecuted() );

    // Conduct an attack and verify nothing happens.
    AbstractArmyAttack attackOrder = new ArmyAttackArmy( aIsAggressor ? armyForA : armyForB, aIsAggressor ? armyForB : armyForA );
    attackOrder.execute( game );
    assertFalse( attackOrder.wasExecuted() );
  }
}
