package kessel.hex.orders.wizard;

import kessel.hex.domain.Army;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.army.AbstractArmyAttack;
import kessel.hex.orders.army.ArmyAttackArmy;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

public class DispelCombatMagicTest extends AbstractCombatSpellTest<DispelCombatMagic>
{
  private static final Logger LOG = Logger.getLogger( DispelCombatMagicTest.class );

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
    DispelCombatMagic oldOrder = new DispelCombatMagic( wizard, pop );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    DispelCombatMagic newOrder = Game.GSON.fromJson( jsonOrder, DispelCombatMagic.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testAgainstArmy()
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
    DispelCombatMagic order = new DispelCombatMagic( wizardForA, armyForB );
    order.execute( game );
    assertTrue( order.wasExecuted() );
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
    DispelCombatMagic order = new DispelCombatMagic( wizardForA, pop );
    order.execute( game );
    assertTrue( order.wasExecuted() );
  }

  @Test
  public void testDeepFogNegated()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    Army armyForB = playerB.getArmies().get( 0 );

    // The defender casts the deep fog spell.
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setBase( armyForA );
    wizardForA.setLevel( 3 );
    armyForA.setLocation( armyForB.getLocation() );
    DeepFog fogOrder = new DeepFog( wizardForA, armyForB );
    fogOrder.execute( game );
    assertTrue( fogOrder.wasExecuted() );

    // The attacker casts dispel combat magic spell.
    Wizard wizardForB = playerB.getWizards().get( 0 );
    wizardForB.setBase( armyForB );
    wizardForB.setLevel( 6 );
    DispelCombatMagic cancelCombatMagicOrder = new DispelCombatMagic( wizardForB, armyForA );
    cancelCombatMagicOrder.execute( game );
    assertTrue( cancelCombatMagicOrder.wasExecuted() );

    // Conduct an attack and verify nothing happens.
    AbstractArmyAttack attackOrder = new ArmyAttackArmy( armyForB, armyForA );
    attackOrder.execute( game );
    assertTrue( attackOrder.wasExecuted() );
  }

  @Test
  public void testPhantomTroopsNegated()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    Army armyForB = playerB.getArmies().get( 0 );

    // The defender casts the phantom troops spell.
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setBase( armyForA );
    wizardForA.setLevel( 2 );
    armyForA.setLocation( armyForB.getLocation() );
    PhantomTroops phantomTroopsOrder = new PhantomTroops( wizardForA, armyForB );

    phantomTroopsOrder.execute( game );
    assertTrue( phantomTroopsOrder.wasExecuted() );

    // The attacker casts dispel combat magic spell.
    Wizard wizardForB = playerB.getWizards().get( 0 );
    wizardForB.setBase( armyForB );
    wizardForB.setLevel( 4 );
    DispelCombatMagic cancelCombatMagicOrder = new DispelCombatMagic( wizardForB, armyForA );
    cancelCombatMagicOrder.execute( game );
    assertTrue( cancelCombatMagicOrder.wasExecuted() );

    // Conduct an attack and verify the phantom troops didn't do anything.
    AbstractArmyAttack attackOrder = new ArmyAttackArmy( armyForB, armyForA );
    attackOrder.execute( game );
    assertTrue( attackOrder.wasExecuted() );
    assertEquals( 581, armyForA.getBaseCombatStrength() );
    assertEquals( 3351, armyForB.getBaseCombatStrength() );
  }

  @Test
  public void testFireballNegated()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    Army armyForB = playerB.getArmies().get( 0 );

    // The defender casts the fireball spell.
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setBase( armyForA );
    wizardForA.setLevel( 2 );
    armyForA.setLocation( armyForB.getLocation() );
    Fireball fireballOrder = new Fireball( wizardForA, armyForB );

    fireballOrder.execute( game );
    assertTrue( fireballOrder.wasExecuted() );

    // The attacker casts dispel combat magic spell.
    Wizard wizardForB = playerB.getWizards().get( 0 );
    wizardForB.setBase( armyForB );
    wizardForB.setLevel( 4 );
    DispelCombatMagic cancelCombatMagicOrder = new DispelCombatMagic( wizardForB, armyForA );
    cancelCombatMagicOrder.execute( game );
    assertTrue( cancelCombatMagicOrder.wasExecuted() );

    // Conduct an attack and verify the phantom troops didn't do anything.
    AbstractArmyAttack attackOrder = new ArmyAttackArmy( armyForB, armyForA );
    attackOrder.execute( game );
    assertTrue( attackOrder.wasExecuted() );
    assertEquals( 581, armyForA.getBaseCombatStrength() );
    assertEquals( 3351, armyForB.getBaseCombatStrength() );
  }
}
