package kessel.hex.orders.army;

import kessel.hex.domain.Army;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.map.Region;
import kessel.hex.util.Tuple;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/** Test moving armies. */
@SuppressWarnings({ "AssignmentToStaticFieldFromInstanceMethod" })
public class ArmyMoveTest extends AbstractArmyMoveTest<ArmyMove>
{
  private static final Logger LOG = Logger.getLogger( ArmyMoveTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army army = playerA.getArmies().get( 0 );
    ArmyMove oldOrder = new ArmyMove( army, Arrays.asList( new Tuple( 1, 0 ), new Tuple( 1, 1 ) ) );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    ArmyMove newOrder = Game.GSON.fromJson( jsonOrder, ArmyMove.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testMoveOffMap()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army army = playerA.getArmies().get( 0 );
    army.setLocation( game.getMap().getLocation( 0, 0 ) );
    ArmyMove order = new ArmyMove( army, Arrays.asList( new Tuple( 1, 0 ), new Tuple( 1, 1 ), new Tuple( 2, 1 ), new Tuple( 3, 1 ) ) );

    assertEquals( 4, order._movementSteps.size() );
    order.execute( game );
    assertEquals( 3, order._movementSteps.size() );
  }

  @Test
  public void testMoveNotAdjacent()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army army = playerA.getArmies().get( 0 );
    army.setLocation( game.getMap().getLocation( 0, 0 ) );
    ArmyMove order = new ArmyMove( army, Arrays.asList( new Tuple( 1, 1 ), new Tuple( 1, 0 ) ) );

    assertEquals( 2, order._movementSteps.size() );
    order.execute( game );
    assertEquals( 1, order._movementSteps.size() );
    assertEquals( army.getLocation().getCoord(), new Tuple( 1, 0 ) );
  }

  @Test
  public void testMoveLegallyAndSeePop()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army army = playerA.getArmies().get( 0 );
    army.setLocation( game.getMap().getLocation( 0, 0 ) );
    ArmyMove order = new ArmyMove( army, Arrays.asList( new Tuple( 1, 0 ), new Tuple( 1, 1 ) ) );
    Region actualRegion = game.getMap().getLocation( 1, 0 ).getRegion();

    // The player knows nothing before hand.
    for ( Region playerRegion : playerA.getKnownRegions() )
    {
      playerRegion.getLocations().clear();
    }
    assertEquals( 24, playerA.getKnownItems().size() );
    order.execute( game );

    // After the move, the player knows locations moved and any pops passed.
    for ( Region playerRegion : playerA.getKnownRegions() )
    {
      if ( playerRegion.getName().equals( actualRegion.getName() ) )
      {
        assertEquals( 2, playerRegion.getLocations().size() );
      }
      else
      {
        assertEquals( 0, playerRegion.getLocations().size() );
      }
    }
    assertEquals( 26, playerA.getKnownItems().size() );

    PopCenter truePopA = game.getPopCenter( 1, 0 );
    PopCenter truePopB = game.getPopCenter( 1, 1 );
    PopCenter foundPopA = playerA.getKnownPopCenter( truePopA.getId() );
    PopCenter foundPopB = playerA.getKnownPopCenter( truePopB.getId() );
    assertNotNull( foundPopA );
    assertNotNull( foundPopB );
    assertEquals( Player.UNKNOWN.getName(), foundPopA.getOwner().getName() );
    assertEquals( PopCenter.VALUE_UNKNOWN, foundPopA.getLevel() );
    assertEquals( Player.UNKNOWN.getName(), foundPopB.getOwner().getName() );
    assertEquals( PopCenter.VALUE_UNKNOWN, foundPopB.getLevel() );
  }
}
