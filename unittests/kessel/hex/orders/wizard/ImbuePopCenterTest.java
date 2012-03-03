package kessel.hex.orders.wizard;

import kessel.hex.domain.Army;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.AbstractOrderTest;
import kessel.hex.util.Tuple;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test improving pop centers with magic! */
public class ImbuePopCenterTest extends AbstractOrderTest<ImbuePopCenter>
{
  private static final Logger LOG = Logger.getLogger( ImbuePopCenterTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    ImbuePopCenter oldOrder = new ImbuePopCenter( playerA.getWizards().get( 0 ) );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    ImbuePopCenter newOrder = Game.GSON.fromJson( jsonOrder, ImbuePopCenter.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testNoPop()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    Army army = playerA.getActiveArmies().get( 0 );
    wizard.setBase( army );
    army.setLocation( game.getMap().getLocation( new Tuple( 0, 2 ) ) );
    ImbuePopCenter order = new ImbuePopCenter( wizard );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testImproved()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    PopCenter pop = playerB.getPopCenters().get( 0 );

    int oldLevel = pop.getLevel();
    Wizard wizard = playerA.getWizards().get( 0 );
    wizard.setBase( pop );
    wizard.setLevel( 3 );
    ImbuePopCenter order = new ImbuePopCenter( wizard );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( oldLevel + 1, pop.getLevel() );
  }
}
