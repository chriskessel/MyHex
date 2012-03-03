package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.AbstractOrderTest;
import kessel.hex.util.Tuple;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test wizard scry. */
public class ScryTest extends AbstractOrderTest<Scry>
{
  private static final Logger LOG = Logger.getLogger( ScryTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( Scry a, Scry b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a.getTarget(), b.getTarget() );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    Scry oldOrder = new Scry( wizard, new Tuple( 1, 0 ) );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    Scry newOrder = Game.GSON.fromJson( jsonOrder, Scry.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testTargetOffMap()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    Scry order = new Scry( wizard, new Tuple( 3, 3 ) );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testStandardRecon()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    wizard.setBase( game.getPlayers().get( 0 ).getCapitol() );
    Scry order = new Scry( wizard, new Tuple( 1, 0 ) );

    assertEquals( 2, playerA.getKnownRegions().size() );
    assertEquals( 24, playerA.getKnownItems().size() );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 2, playerA.getKnownRegions().size() );
    assertEquals( 2, playerA.getKnownRegions().get( 0 ).getLocations().size() );
    assertEquals( 44, playerA.getKnownItems().size() );
    assertNotNull( playerA.getWizard( wizard.getId() ) );
  }
}
