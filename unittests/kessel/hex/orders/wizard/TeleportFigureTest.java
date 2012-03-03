package kessel.hex.orders.wizard;

import kessel.hex.domain.Army;
import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.King;
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

public class TeleportFigureTest extends AbstractOrderTest<TeleportFigure>
{
  private static final Logger LOG = Logger.getLogger( TeleportFigureTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( TeleportFigure a, TeleportFigure b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a.getNewBase().getId(), b._jsonNewBaseId.intValue() );
    assertEquals( a.getTarget().getId(), b._jsonTargetId.intValue() );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    PopCenter oldPopCenter = game.getPopCenter( new Tuple( 0, 0 ) );
    Wizard wizard = playerA.getWizards().get( 0 );
    Diplomat diplomat = playerA.getDiplomats().get( 0 );
    TeleportFigure oldOrder = new TeleportFigure( wizard, diplomat, oldPopCenter );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    TeleportFigure newOrder = Game.GSON.fromJson( jsonOrder, TeleportFigure.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testMoveToMyArmy()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    Diplomat diplomat = playerA.getDiplomats().get( 0 );
    Army army = playerA.getActiveArmies().get( 1 );
    assertFalse( diplomat.getBase().equals( army ) );

    TeleportFigure order = new TeleportFigure( wizard, diplomat, army );
    order.execute( game );

    assertTrue( order.wasExecuted() );
    assertSame( army, diplomat.getBase() );
    assertFalse( army.equals( wizard.getBase() ) );
  }

  @Test
  public void testMoveToPopCenter()
  {
    Game game = GameTest.createSimpleGame();
    PopCenter popCenter = game.getPopCenter( new Tuple( 1, 0 ) );
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    Diplomat diplomat = playerA.getDiplomats().get( 0 );
    assertFalse( diplomat.getBase().equals( popCenter ) );
    wizard.setLevel( 2 );

    TeleportFigure order = new TeleportFigure( wizard, diplomat, popCenter );
    order.execute( game );

    assertTrue( order.wasExecuted() );
    assertSame( popCenter, diplomat.getBase() );
    assertFalse( popCenter.equals( wizard.getBase() ) );
  }

  @Test
  public void testMoveToNotMyArmy()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizard = playerA.getWizards().get( 0 );
    Diplomat diplomat = playerA.getDiplomats().get( 0 );
    Army army = playerB.getActiveArmies().get( 1 );
    assertFalse( diplomat.getBase().equals( army ) );
    wizard.setLevel( 2 );

    TeleportFigure order = new TeleportFigure( wizard, diplomat, army );
    order.execute( game );

    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testMoveKingShouldFail()
  {
    Game game = GameTest.createSimpleGame();
    PopCenter popCenter = game.getPopCenter( new Tuple( 1, 0 ) );
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    King king = playerA.getKing();
    wizard.setLevel( 2 );

    TeleportFigure order = new TeleportFigure( wizard, king, popCenter );
    order.execute( game );

    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testMoveNotMyFigure()
  {
    Game game = GameTest.createSimpleGame();
    PopCenter popCenter = game.getPopCenter( new Tuple( 1, 1 ) );
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizard = playerA.getWizards().get( 0 );
    Diplomat diplomat = playerB.getDiplomats().get( 0 );
    assertFalse( diplomat.getBase().equals( popCenter ) );
    wizard.setLevel( 2 );

    TeleportFigure order = new TeleportFigure( wizard, diplomat, popCenter );
    order.execute( game );

    assertFalse( order.wasExecuted() );
  }
}
