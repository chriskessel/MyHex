package kessel.hex.orders.army;

import kessel.hex.domain.Army;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.util.Tuple;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Test armies searching for pop centers. Most relevant tests are done in MoveArmyTest. This class just tests movement feature specific to
 * the search order.
 */
@SuppressWarnings({ "AssignmentToStaticFieldFromInstanceMethod" })
public class ArmySearchTest extends AbstractArmyMoveTest<ArmySearch>
{
  private static final Logger LOG = Logger.getLogger( ArmySearchTest.class );

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
    ArmySearch oldOrder = new ArmySearch( army, Arrays.asList( new Tuple( 1, 0 ), new Tuple( 1, 1 ) ) );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    ArmySearch newOrder = Game.GSON.fromJson( jsonOrder, ArmySearch.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testMoveLegallyAndSeePop()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army army = playerA.getArmies().get( 0 );
    army.setLocation( game.getMap().getLocation( 0, 0 ) );
    ArmySearch order = new ArmySearch( army, Arrays.asList( new Tuple( 1, 0 ), new Tuple( 1, 1 ) ) );

    order.execute( game );

    // Verify the army stopped on the first hex since it has a pop.
    assertEquals( army.getLocation().getCoord(), new Tuple( 1, 0 ) );
  }
}
