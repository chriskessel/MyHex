package kessel.hex.orders.army;

import kessel.hex.domain.Army;
import kessel.hex.domain.ArmyUnit;
import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.TroopType;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.AbstractOrderTest;
import kessel.hex.orders.Order;
import kessel.hex.orders.wizard.MagicDome;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test army attacking. */
@SuppressWarnings({ "AssignmentToStaticFieldFromInstanceMethod" })
public class ArmyAttackTest extends AbstractOrderTest<AbstractArmyAttack>
{
  private static final Logger LOG = Logger.getLogger( ArmyAttackTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( AbstractArmyAttack a, AbstractArmyAttack b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a.getTarget().getId(), b._jsonTargetId.intValue() );
  }

  @Test
  public void testArmyVsArmyPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    Army armyForB = playerB.getArmies().get( 1 );

    ArmyAttackArmy oldOrder = new ArmyAttackArmy( armyForA, armyForB );
    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    ArmyAttackArmy newOrder = Game.GSON.fromJson( jsonOrder, ArmyAttackArmy.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testArmyVsPopPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );

    ArmyAttackPop oldOrder = new ArmyAttackPop( armyForA, playerB.getCapitol() );
    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    ArmyAttackPop newOrder = Game.GSON.fromJson( jsonOrder, ArmyAttackPop.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testGoodTargetBadLocation()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    armyForA.setLocation( playerB.getCapitol().getLocation() );
    Army armyForB = playerB.getArmies().get( 0 );
    armyForB.setLocation( playerA.getCapitol().getLocation() );

    ArmyAttackArmy order = new ArmyAttackArmy( armyForA, armyForB );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testAttackerTooSmall()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    armyForA.setLocation( playerB.getCapitol().getLocation() );
    Army armyForB = playerB.getArmies().get( 0 );
    armyForA.getUnits().clear();

    ArmyAttackArmy order = new ArmyAttackArmy( armyForA, armyForB );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testDefenderTooSmall()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    armyForA.setLocation( playerB.getCapitol().getLocation() );
    Army armyForB = playerB.getArmies().get( 0 );
    armyForB.getUnits().clear();

    ArmyAttackArmy order = new ArmyAttackArmy( armyForA, armyForB );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testArmyAttackPopSucceedsWithDegrade()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    armyForA.setLocation( playerB.getCapitol().getLocation() );
    PopCenter popForB = playerB.getPopCenters().get( 0 );

    ArmyAttackPop order = new ArmyAttackPop( armyForA, popForB );
    AbstractArmyAttack.DEGRADE_CHANCE = 0;
    int popLevel = popForB.getLevel();
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( playerA, popForB.getOwner() );
    assertTrue( playerA.getPopCenters().contains( popForB ) );
    assertFalse( playerB.getPopCenters().contains( popForB ) );
    assertEquals( popLevel, popForB.getLevel() );
  }

  @Test
  public void testArmyAttackPopSucceedsNoDegrade()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    armyForA.setLocation( playerB.getCapitol().getLocation() );
    PopCenter popForB = playerB.getPopCenters().get( 0 );

    ArmyAttackPop order = new ArmyAttackPop( armyForA, popForB );
    AbstractArmyAttack.DEGRADE_CHANCE = 100;
    int popLevel = popForB.getLevel();
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( playerA, popForB.getOwner() );
    assertTrue( playerA.getPopCenters().contains( popForB ) );
    assertFalse( playerB.getPopCenters().contains( popForB ) );
    assertEquals( popLevel - 1, popForB.getLevel() );
  }

  @Test
  public void testArmyAttackPopWithDomeFails()
  {
    // First, shield the pop.
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardForB = playerB.getWizards().get( 0 );
    PopCenter popForB = playerB.getPopCenters().get( 0 );
    wizardForB.setLevel( 6 );
    MagicDome domeOrder = new MagicDome( wizardForB, popForB );
    domeOrder.execute( game );
    assertTrue( domeOrder.wasExecuted() );

    // Now try the attack.
    Army armyForA = playerA.getArmies().get( 0 );
    armyForA.setLocation( playerB.getCapitol().getLocation() );
    ArmyAttackPop attackOrder = new ArmyAttackPop( armyForA, popForB );
    attackOrder.execute( game );
    assertFalse( attackOrder.wasExecuted() );
  }

  @Test
  public void testArmyAttackPopFails()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    armyForA.setLocation( playerB.getCapitol().getLocation() );
    PopCenter popForB = playerB.getPopCenters().get( 0 );
    popForB.setLevel( 99 );

    ArmyAttackPop order = new ArmyAttackPop( armyForA, popForB );
    int popLevel = popForB.getLevel();
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( playerB, popForB.getOwner() );
    assertFalse( playerA.getPopCenters().contains( popForB ) );
    assertTrue( playerB.getPopCenters().contains( popForB ) );
    assertEquals( popLevel, popForB.getLevel() );
  }

  @Test
  public void testArmiesAttackEachOther()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    armyForA.setLocation( playerB.getCapitol().getLocation() );
    Army armyForB = playerB.getArmies().get( 0 );
    armyForB.getUnits().remove( 0 );

    int armyApreMorale = armyForA.getUnits().get( 0 ).getMorale();
    int armyBpreMorale = armyForB.getUnits().get( 0 ).getMorale();
    ArmyAttackArmy a_attack_b = new ArmyAttackArmy( armyForA, armyForB );
    a_attack_b.execute( game );
    ArmyAttackArmy b_attack_a = new ArmyAttackArmy( armyForB, armyForA );
    b_attack_a.execute( game );

    // Verify only one of the orders executed, but that both saw the attack was part of their orders completed.
    assertTrue( a_attack_b.wasExecuted() || b_attack_a.wasExecuted() );
    assertFalse( a_attack_b.wasExecuted() && b_attack_a.wasExecuted() );
    assertTrue( armyForA.getOrdersExecuted().contains( a_attack_b ) );
    assertTrue( armyForB.getOrdersExecuted().contains( b_attack_a ) );
    assertFalse( a_attack_b.attackerWon() );
    Order armyBAttackOrder = armyForB.getOrdersExecuted().get( 0 );
    assertTrue( ((AbstractArmyAttack) armyBAttackOrder).attackerWon() );
    assertEquals( armyApreMorale - 10, armyForA.getUnits().get( 0 ).getMorale() );
    assertEquals( armyBpreMorale + 10, armyForB.getUnits().get( 0 ).getMorale() );
  }

  @Test
  public void testArmyAttacksTwice()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    armyForA.setLocation( playerB.getCapitol().getLocation() );
    Army armyForB = playerB.getArmies().get( 0 );
    armyForB.getUnits().remove( 0 );

    AbstractArmyAttack a_attack_b_1 = new ArmyAttackArmy( armyForA, armyForB );
    a_attack_b_1.execute( game );
    AbstractArmyAttack a_attack_b_2 = new ArmyAttackArmy( armyForA, armyForB );
    a_attack_b_2.execute( game );

    // Verify only one of the orders executed.
    assertTrue( a_attack_b_1.wasExecuted() && !a_attack_b_2.wasExecuted() );
    assertEquals( 1, armyForA.getOrdersExecuted().size() );
  }

  @Test
  public void testAttackPopButArmyRetreated()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    armyForA.adjustMorale( 50 );
    armyForA.setLocation( playerA.getCapitol().getLocation() );
    Army armyForB = playerB.getArmies().get( 0 );
    armyForB.getUnits().remove( 0 );
    armyForB.setLocation( playerA.getCapitol().getLocation() );
    PopCenter popForA = playerA.getPopCenters().get( 0 );

    AbstractArmyAttack a_attack_b = new ArmyAttackArmy( armyForA, armyForB );
    a_attack_b.execute( game );
    AbstractArmyAttack b_attack_pop = new ArmyAttackPop( armyForB, popForA );
    b_attack_pop.execute( game );

    // Verify only one of the orders executed.
    assertTrue( a_attack_b.wasExecuted() );
    assertTrue( a_attack_b.attackerWon() );
    assertFalse( b_attack_pop.wasExecuted() );
  }

  @Test
  public void testArmyAttackArmyAttackerWins()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    armyForA.setLocation( playerB.getCapitol().getLocation() );
    armyForA.adjustMorale( 50 );
    Army armyForB = playerB.getArmies().get( 0 );
    armyForB.getUnits().remove( 0 );

    AbstractArmyAttack order = new ArmyAttackArmy( armyForA, armyForB );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertTrue( order.attackerWon() );
  }

  @Test
  public void testArmyAttackArmyAttackerLoses()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    armyForA.setLocation( playerB.getCapitol().getLocation() );
    Army armyForB = playerB.getArmies().get( 0 );
    armyForA.getUnits().remove( 0 );

    AbstractArmyAttack order = new ArmyAttackArmy( armyForA, armyForB );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertFalse( order.attackerWon() );
  }

  @Test
  public void testArmyAttackPopAndCaptureFigures()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    armyForA.setLocation( playerB.getCapitol().getLocation() );
    PopCenter popForB = playerB.getPopCenters().get( 0 );
    Diplomat diplomatForB = playerB.getDiplomats().get( 0 );
    diplomatForB.setBase( popForB );
    Wizard wizardForB = playerB.getWizards().get( 0 );
    wizardForB.setBase( popForB );

    AbstractArmyAttack order = new ArmyAttackPop( armyForA, popForB );
    AbstractArmyAttack.CAPTURE_CHANCE = 100;
    assertTrue( playerB.getDiplomats().contains( diplomatForB ) );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertFalse( playerB.getDiplomats().contains( diplomatForB ) );
    assertTrue( playerB.getWizards().contains( wizardForB ) );
  }

  @Test
  public void testArmyAttackPopAndNoCaptureFigures()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    PopCenter popToAttack = game.getPopCenter( 1, 1 );
    armyForA.setLocation( popToAttack.getLocation() );
    Diplomat diplomatForB = playerB.getDiplomats().get( 0 );
    diplomatForB.setBase( popToAttack );

    AbstractArmyAttack order = new ArmyAttackPop( armyForA, popToAttack );
    AbstractArmyAttack.CAPTURE_CHANCE = 0;
    assertTrue( playerB.getDiplomats().contains( diplomatForB ) );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertTrue( playerB.getDiplomats().contains( diplomatForB ) );
    assertEquals( playerB.getCapitol(), diplomatForB.getBase() );
  }

  @Test
  public void testArmyAttackArmyAndCaptureFigures()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    armyForA.setLocation( playerB.getCapitol().getLocation() );
    Army armyForB = playerB.getArmies().get( 0 );
    Diplomat diplomatForB = playerB.getDiplomats().get( 0 );
    diplomatForB.setBase( armyForB );
    Wizard wizardForB = playerB.getWizards().get( 0 );
    wizardForB.setBase( armyForB );

    // Make ArmyA big enough to completely destroy armyB.
    for ( int i = 0; i < 10; i++ )
    {
      armyForA.addUnit( new ArmyUnit( new TroopType( "foo", 1000, 1.0, 0.5 ) ) );
    }
    armyForB.getUnits().remove( 0 );
    armyForB.getUnits().remove( 0 );

    AbstractArmyAttack.CAPTURE_CHANCE = 100;
    assertTrue( playerB.getDiplomats().contains( diplomatForB ) );
    assertTrue( playerB.getArmies().contains( armyForB ) );
    AbstractArmyAttack order = new ArmyAttackArmy( armyForA, armyForB );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertTrue( order.attackerWon() );
    assertEquals( playerB.getCapitol().getLocation(), armyForB.getLocation() );
    assertFalse( playerB.getDiplomats().contains( diplomatForB ) );
    assertFalse( playerB.getWizards().contains( wizardForB ) );
  }

  @Test
  public void testArmyAttackArmyAndNoCaptureFigures()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    armyForA.setLocation( playerB.getCapitol().getLocation() );
    Army armyForB = playerB.getArmies().get( 0 );
    Diplomat diplomatForB = playerB.getDiplomats().get( 0 );
    diplomatForB.setBase( armyForB );

    // Make ArmyA big enough to completely destroy armyB.
    for ( int i = 0; i < 10; i++ )
    {
      armyForA.addUnit( new ArmyUnit( new TroopType( "foo", 1000, 1.0, 0.5 ) ) );
    }
    armyForB.getUnits().remove( 0 );
    armyForB.getUnits().remove( 0 );

    AbstractArmyAttack.CAPTURE_CHANCE = 0;
    assertTrue( playerB.getDiplomats().contains( diplomatForB ) );
    assertTrue( playerB.getArmies().contains( armyForB ) );
    AbstractArmyAttack order = new ArmyAttackArmy( armyForA, armyForB );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertTrue( order.attackerWon() );
    assertEquals( playerB.getCapitol().getLocation(), armyForB.getLocation() );
    assertTrue( playerB.getDiplomats().contains( diplomatForB ) );
    assertEquals( playerB.getCapitol(), diplomatForB.getBase() );
  }
}
