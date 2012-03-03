package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;
import kessel.hex.util.Tuple;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

public class SubvertHamletTest extends AbstractSubvertPopCenterTest<SubvertHamlet>
{
  private static final Logger LOG = Logger.getLogger( SubvertHamletTest.class );

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
    SubvertHamlet oldOrder = new SubvertHamlet( wizard, pop );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    SubvertHamlet newOrder = Game.GSON.fromJson( jsonOrder, SubvertHamlet.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testNotInSameRegion()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 0 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    playerB.setCapitol( game.getPopCenter( new Tuple( 2, 2 ) ) ); // playerB must stop using the hamlet as the capitol.
    PopCenter hamlet = game.getPopCenter( new Tuple( 1, 0 ) );
    assertEquals( PopCenter.PopType.Hamlet, hamlet.getType() );

    SubvertHamlet order = new SubvertHamlet( wizardForA, hamlet );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testTooBig()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setBase( game.getPopCenter( new Tuple( 2, 2 ) ) ); // put the wizard in the same region as the hamlet
    PopCenter town = game.getPopCenter( new Tuple( 1, 1 ) );
    assertEquals( PopCenter.PopType.Town, town.getType() );

    SubvertHamlet order = new SubvertHamlet( wizardForA, town );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testIsCapitol()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setBase( game.getPopCenter( new Tuple( 1, 1 ) ) ); // put the wizard in the same region as the hamlet
    PopCenter hamlet = game.getPopCenter( new Tuple( 1, 0 ) );

    SubvertHamlet order = new SubvertHamlet( wizardForA, hamlet );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testIsProtected()
  {
    // Cast a shield spell first.
    Game game = GameTest.createSimpleGame();
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardForB = playerB.getWizards().get( 0 );
    wizardForB.setLevel( 3 );
    ShieldRegion shieldOrder = new ShieldRegion( wizardForB );
    shieldOrder.execute( game );
    assertTrue( shieldOrder.wasExecuted() );

    // Now try to subvert.
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setBase( game.getPopCenter( new Tuple( 2, 2 ) ) ); // put the wizard in the same region as the hamlet
    playerB.setCapitol( game.getPopCenter( new Tuple( 2, 2 ) ) ); // playerB must stop using the hamlet as the capitol.
    PopCenter hamlet = game.getPopCenter( new Tuple( 1, 0 ) );
    assertEquals( PopCenter.PopType.Hamlet, hamlet.getType() );
    wizardForA.setLevel( 6 );

    SubvertHamlet order = new SubvertHamlet( wizardForA, hamlet );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testSuccess()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setBase( game.getPopCenter( new Tuple( 2, 2 ) ) ); // put the wizard in the same region as the hamlet
    playerB.setCapitol( game.getPopCenter( new Tuple( 2, 2 ) ) ); // playerB must stop using the hamlet as the capitol.
    PopCenter hamlet = game.getPopCenter( new Tuple( 1, 0 ) );
    assertEquals( PopCenter.PopType.Hamlet, hamlet.getType() );
    wizardForA.setLevel( 6 );

    SubvertHamlet order = new SubvertHamlet( wizardForA, hamlet );
    assertTrue( playerB.getPopCenters().contains( hamlet ) );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( playerA, hamlet.getOwner() );
    assertFalse( playerB.getPopCenters().contains( hamlet ) );
  }
}
