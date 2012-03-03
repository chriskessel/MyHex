package kessel.hex.orders.wizard;

import kessel.hex.domain.Army;
import kessel.hex.domain.ArmyUnit;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.TroopType;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.AbstractOrderTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

@SuppressWarnings({ "AssignmentToStaticFieldFromInstanceMethod" })
public class PlagueArmyTest extends AbstractOrderTest<PlagueArmy>
{
  private static final Logger LOG = Logger.getLogger( PlagueArmyTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( PlagueArmy a, PlagueArmy b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a.getTarget().getId(), b._jsonTargetId.intValue() );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizard = playerA.getWizards().get( 0 );
    Army armyForB = playerB.getArmies().get( 0 );
    PlagueArmy oldOrder = new PlagueArmy( wizard, armyForB );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    PlagueArmy newOrder = Game.GSON.fromJson( jsonOrder, PlagueArmy.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testNotSameLocation()
  {
    // Cast the plague
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    Army armyForB = playerB.getArmies().get( 0 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setBase( armyForA );

    PlagueArmy order = new PlagueArmy( wizardForA, armyForB );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testTooSmall()
  {
    // Cast the plague
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    Army armyForB = playerB.getArmies().get( 0 );
    armyForA.setLocation( armyForB.getLocation() );
    armyForB.getUnits().clear();
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setBase( armyForA );

    PlagueArmy order = new PlagueArmy( wizardForA, armyForB );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testWorksNotDestroyed()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    Army armyForB = playerB.getArmies().get( 0 );
    armyForB.getUnits().clear();
    armyForB.addUnit( new ArmyUnit( TroopType.OGRE ) );
    armyForB.addUnit( new ArmyUnit( TroopType.OGRE ) );
    armyForB.addUnit( new ArmyUnit( TroopType.OGRE ) );
    armyForB.addUnit( new ArmyUnit( TroopType.OGRE ) );
    armyForB.addUnit( new ArmyUnit( TroopType.OGRE ) );
    armyForA.setLocation( armyForB.getLocation() );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setBase( armyForA );
    wizardForA.setLevel( 7 );

    assertEquals( 8500, armyForB.getBaseCombatStrength() );
    PlagueArmy order = new PlagueArmy( wizardForA, armyForB );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 1696, armyForB.getBaseCombatStrength() );
  }

  @Test
  public void testWorksIsDestroyedAndKillFigure()
  {
    PlagueArmy.FIGURE_PLAGUE_CHANCE = 100;

    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    Army armyForB = playerB.getArmies().get( 0 );
    armyForB.getUnits().clear();
    armyForB.addUnit( new ArmyUnit( TroopType.OGRE ) );
    armyForB.addUnit( new ArmyUnit( TroopType.OGRE ) );
    armyForB.addUnit( new ArmyUnit( TroopType.OGRE ) );
    armyForA.setLocation( armyForB.getLocation() );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setBase( armyForA );
    wizardForA.setLevel( 7 );
    Wizard wizardForB = playerB.getWizards().get( 0 );
    wizardForB.setBase( armyForB );

    PlagueArmy order = new PlagueArmy( wizardForA, armyForB );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 0, armyForB.getBaseCombatStrength() );
    assertFalse( playerB.getWizards().contains( wizardForB ) );
  }
}
