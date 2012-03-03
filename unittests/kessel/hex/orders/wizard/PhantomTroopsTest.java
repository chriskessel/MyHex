package kessel.hex.orders.wizard;

import kessel.hex.domain.Army;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.army.AbstractArmyAttack;
import kessel.hex.orders.army.ArmyAttackArmy;
import kessel.hex.orders.army.ArmyAttackPop;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

public class PhantomTroopsTest extends AbstractCombatSpellTest<PhantomTroops>
{
  private static final Logger LOG = Logger.getLogger( PhantomTroopsTest.class );

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
    Wizard wizard = playerA.getWizards().get( 0 );
    PopCenter pop = playerB.getPopCenters().get( 0 );
    PhantomTroops oldOrder = new PhantomTroops( wizard, pop );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    PhantomTroops newOrder = Game.GSON.fromJson( jsonOrder, PhantomTroops.class );
    doEqualsTest( oldOrder, newOrder );
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
    // Cast the phantom troops spell.
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    armyForA.setLocation( playerB.getCapitol().getLocation() );
    Army armyForB = playerB.getArmies().get( 0 );
    armyForB.getUnits().remove( 0 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setBase( armyForA );
    PhantomTroops order = new PhantomTroops( wizardForA, armyForB );
    order.execute( game );
    assertTrue( order.wasExecuted() );

    // Conduct an attack and verify the damage is lower than expected.
    AbstractArmyAttack a_attack_b = new ArmyAttackArmy( aIsAggressor ? armyForA : armyForB, aIsAggressor ? armyForB : armyForA );
    a_attack_b.execute( game );
    assertTrue( a_attack_b.wasExecuted() );
    assertEquals( 1878, armyForA.getBaseCombatStrength() );
  }

  @Test
  public void testAgainstPop()
  {
    // Cast the phantom troops spell.
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    armyForA.setLocation( playerB.getCapitol().getLocation() );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setBase( armyForA );
    PopCenter pop = playerB.getCapitol();
    PhantomTroops order = new PhantomTroops( wizardForA, pop );
    order.execute( game );
    assertTrue( order.wasExecuted() );

    // Conduct an attack and verify the damage is lower than expected.
    AbstractArmyAttack a_attack_b = new ArmyAttackPop( armyForA, pop );
    a_attack_b.execute( game );
    assertTrue( a_attack_b.wasExecuted() );
    assertEquals( 3344, armyForA.getBaseCombatStrength() );
  }
}
