package kessel.hex.orders.wizard;

import kessel.hex.domain.Army;
import kessel.hex.domain.ArmyUnit;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.TroopType;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.AbstractOrderTest;
import kessel.hex.util.Tuple;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test moving wizards. */
@SuppressWarnings({ "AssignmentToStaticFieldFromInstanceMethod" })
public class TeleportSelfTest extends AbstractOrderTest<TeleportSelf>
{
  private static final Logger LOG = Logger.getLogger( TeleportSelfTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    PopCenter oldPopCenter = game.getPopCenter( new Tuple( 0, 0 ) );
    Wizard wizard = new Wizard( game.generateUniqueId(), "Bob", 3, oldPopCenter );
    Player playerA = game.getPlayers().get( 0 );
    playerA.add( wizard );
    TeleportSelf oldOrder = new TeleportSelf( wizard, oldPopCenter );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    TeleportSelf newOrder = Game.GSON.fromJson( jsonOrder, TeleportSelf.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testMoveToPopCenter()
  {
    Game game = GameTest.createSimpleGame();
    game.setTurn( 4 );
    PopCenter oldPopCenter = game.getPopCenter( new Tuple( 0, 0 ) );
    Wizard wizard = new Wizard( game.generateUniqueId(), "Bob", 3, oldPopCenter );
    assertSame( oldPopCenter, wizard.getBase() );
    Player playerA = game.getPlayers().get( 0 );
    playerA.add( wizard );

    // Move the Wizard to a new pop center.
    PopCenter newPopCenter = game.getPopCenter( new Tuple( 1, 0 ) );
    assertNotSame( oldPopCenter, newPopCenter );
    TeleportSelf order = new TeleportSelf( wizard, newPopCenter );
    order.execute( game );

    assertSame( wizard, playerA.getWizard( wizard.getId() ) );
    assertSame( newPopCenter, wizard.getBase() );
  }

  @Test
  public void testMoveToMyArmy()
  {
    Game game = GameTest.createSimpleGame();
    game.setTurn( 4 );
    PopCenter oldPopCenter = game.getPopCenter( new Tuple( 0, 0 ) );
    Wizard wizard = new Wizard( game.generateUniqueId(), "Bob", 3, oldPopCenter );
    assertSame( oldPopCenter, wizard.getBase() );
    Player playerA = game.getPlayers().get( 0 );
    playerA.add( wizard );
    Army army = new Army( game.generateUniqueId(), "ArmyA", 3, game.getMap().getLocation( 1, 1 ) );
    army.addUnit( new ArmyUnit( TroopType.LEVY ) );
    playerA.add( army );

    // Move the Wizard to army.
    TeleportSelf order = new TeleportSelf( wizard, army );
    order.execute( game );

    assertSame( wizard, playerA.getWizard( wizard.getId() ) );
    assertSame( army, wizard.getBase() );
  }

  @Test
  public void testMoveToNotMyArmy()
  {
    Game game = GameTest.createSimpleGame();
    game.setTurn( 4 );
    PopCenter oldPopCenter = game.getPopCenter( new Tuple( 0, 0 ) );
    Wizard wizard = new Wizard( game.generateUniqueId(), "Bob", 3, oldPopCenter );
    assertSame( oldPopCenter, wizard.getBase() );
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    playerA.add( wizard );
    Army army = new Army( game.generateUniqueId(), "ArmyB", 3, game.getMap().getLocation( 1, 1 ) );
    playerB.add( army );

    // Move the Wizard to another player's army, which is invalid and should do nothing.
    TeleportSelf order = new TeleportSelf( wizard, army );
    order.execute( game );

    assertSame( wizard, playerA.getWizard( wizard.getId() ) );
    assertSame( oldPopCenter, wizard.getBase() );
  }
}
