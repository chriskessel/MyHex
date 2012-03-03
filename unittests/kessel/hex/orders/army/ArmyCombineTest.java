package kessel.hex.orders.army;

import kessel.hex.domain.Army;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test army combining. */
@SuppressWarnings({ "AssignmentToStaticFieldFromInstanceMethod" })
public class ArmyCombineTest extends AbstractArmyTransferTest<ArmyCombine>
{
  private static final Logger LOG = Logger.getLogger( ArmyCombineTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army armyOneForA = playerA.getArmies().get( 0 );
    Army armyTwoForA = playerA.getArmies().get( 1 );

    ArmyCombine oldOrder = new ArmyCombine( armyOneForA, armyTwoForA );
    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    ArmyCombine newOrder = Game.GSON.fromJson( jsonOrder, ArmyCombine.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testSubjectIsInactive()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army armyOneForA = playerA.getArmies().get( 0 );
    Army armyTwoForA = playerA.getArmies().get( 1 );
    armyOneForA.getUnits().clear();
    armyTwoForA.setLocation( armyOneForA.getLocation() );

    ArmyCombine order = new ArmyCombine( armyOneForA, armyTwoForA );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testCombineActives()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army armyOneForA = playerA.getArmies().get( 0 );
    Army armyTwoForA = playerA.getArmies().get( 1 );
    armyTwoForA.setLocation( armyOneForA.getLocation() );

    ArmyCombine order = new ArmyCombine( armyOneForA, armyTwoForA );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 0, armyOneForA.getUnits().size() );
    assertEquals( 6, armyTwoForA.getUnits().size() );
    assertFalse( armyOneForA.isActive() );
  }

  @Test
  public void testCombineWithInactive()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army armyOneForA = playerA.getArmies().get( 0 );
    Army armyTwoForA = playerA.getArmies().get( 1 );
    armyTwoForA.getUnits().clear();
    armyTwoForA.setLocation( armyOneForA.getLocation() );

    ArmyCombine order = new ArmyCombine( armyOneForA, armyTwoForA );
    assertFalse( armyTwoForA.isActive() );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 0, armyOneForA.getUnits().size() );
    assertEquals( 4, armyTwoForA.getUnits().size() );
    assertFalse( armyOneForA.isActive() );
    assertTrue( armyTwoForA.isActive() );
  }

}

