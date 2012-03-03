package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.AbstractOrderTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

public class FireStormTest extends AbstractOrderTest<FireStorm>
{
  private static final Logger LOG = Logger.getLogger( FireStormTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( FireStorm a, FireStorm b ) throws Exception
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
    PopCenter pop = playerB.getPopCenters().get( 0 );
    FireStorm oldOrder = new FireStorm( wizard, pop );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    FireStorm newOrder = Game.GSON.fromJson( jsonOrder, FireStorm.class );
    doEqualsTest( oldOrder, newOrder );
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

    // Now try to damage the pop.
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    PopCenter pop = playerB.getPopCenters().get( 0 );
    pop.getLocation().setRegion( wizardForA.getLocation().getRegion() );
    wizardForA.setLevel( 8 );

    FireStorm order = new FireStorm( wizardForA, pop );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testNotInSameRegion()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizard = playerA.getWizards().get( 0 );
    PopCenter pop = playerB.getPopCenters().get( 0 );

    FireStorm order = new FireStorm( wizard, pop );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testIsCity()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    wizard.setBase( game.getPopCenter( 1, 0 ) );
    PopCenter pop = game.getPopCenter( 2, 2 );

    FireStorm order = new FireStorm( wizard, pop );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testIsCapitol()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    wizard.setBase( game.getPopCenter( 1, 1 ) );
    PopCenter pop = game.getPopCenter( 1, 0 );

    FireStorm order = new FireStorm( wizard, pop );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testFireStormWorks()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    playerB.setCapitol( game.getPopCenter( 2, 2 ) );
    playerB.getKing().setBase( playerB.getCapitol() );
    Wizard wizard = playerA.getWizards().get( 0 );
    PopCenter pop = game.getPopCenter( 1, 0 );
    pop.getLocation().setRegion( wizard.getLocation().getRegion() );
    wizard.setLevel( 8 );

    FireStorm order = new FireStorm( wizard, pop );
    int oldLevel = pop.getLevel();
    assertEquals( 16, playerB.getFigures().size() );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( pop.getLevel(), oldLevel - (oldLevel / 2) );
    assertEquals( 1, playerB.getFigures().size() );
  }
}
