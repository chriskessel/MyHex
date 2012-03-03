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

public class FireballTest extends AbstractCombatSpellTest<Fireball>
{
  private static final Logger LOG = Logger.getLogger( FireballTest.class );

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
    Fireball oldOrder = new Fireball( wizard, pop );

    wizard.setLevel( 1 );
    assertEquals( Fireball.BASE_DAMAGE, oldOrder.getValue() );
    wizard.setLevel( 2 );
    assertEquals( Fireball.BASE_DAMAGE * 3, oldOrder.getValue() );
    wizard.setLevel( 3 );
    assertEquals( Fireball.BASE_DAMAGE * 6, oldOrder.getValue() );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    Fireball newOrder = Game.GSON.fromJson( jsonOrder, Fireball.class );
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
    // Cast the fireball.
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    armyForA.setLocation( playerB.getCapitol().getLocation() );
    Army armyForB = playerB.getArmies().get( 0 );
    armyForB.getUnits().remove( 0 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setBase( armyForA );
    Fireball order = new Fireball( wizardForA, armyForB );
    order.execute( game );
    assertTrue( order.wasExecuted() );

    // Conduct an attack and verify the damage is lower than expected.
    AbstractArmyAttack a_attack_b = new ArmyAttackArmy( aIsAggressor ? armyForA : armyForB, aIsAggressor ? armyForB : armyForA );
    a_attack_b.execute( game );
    assertTrue( a_attack_b.wasExecuted() );
    assertEquals( 1282, armyForB.getBaseCombatStrength() );
  }

  @Test
  public void testAgainstPop()
  {
    // Cast the fireball.
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    armyForA.setLocation( playerB.getCapitol().getLocation() );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setBase( armyForA );
    PopCenter pop = playerB.getCapitol();
    Fireball order = new Fireball( wizardForA, pop );
    order.execute( game );
    assertTrue( order.wasExecuted() );

    // Conduct an attack and verify the attack works where it would have failed without the fireball.
    pop.setLevel( 5 );
    AbstractArmyAttack a_attack_b = new ArmyAttackPop( armyForA, pop );
    a_attack_b.execute( game );
    assertTrue( a_attack_b.wasExecuted() );
    assertTrue( a_attack_b.attackerWon() );
  }

  @Test
  public void testWizardInPop()
  {
    // Cast the fireball.
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    armyForA.setLocation( playerB.getCapitol().getLocation() );
    Wizard wizardForB = playerB.getWizards().get( 0 );
    PopCenter pop = playerB.getCapitol();
    wizardForB.setBase( pop );
    Fireball order = new Fireball( wizardForB, armyForA );
    order.execute( game );
    assertTrue( order.wasExecuted() );

    // Conduct an attack and verify the attack works where it would have failed without the fireball.
    pop.setLevel( 4 );
    AbstractArmyAttack a_attack_b = new ArmyAttackPop( armyForA, pop );
    a_attack_b.execute( game );
    assertTrue( a_attack_b.wasExecuted() );
    assertTrue( a_attack_b.attackerWon() );
  }
}
