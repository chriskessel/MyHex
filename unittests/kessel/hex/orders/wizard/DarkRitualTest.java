package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;
import kessel.hex.map.ControlLevel;
import kessel.hex.map.Location;
import kessel.hex.orders.AbstractOrderTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

public class DarkRitualTest extends AbstractOrderTest<DarkRitual>
{
  private static final Logger LOG = Logger.getLogger( DarkRitualTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizardForA = game.getPlayers().get( 0 ).getWizards().get( 0 );

    DarkRitual oldOrder = new DarkRitual( wizardForA );
    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    DarkRitual newOrder = Game.GSON.fromJson( jsonOrder, DarkRitual.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testDarkRitual()
  {
    // Set the wizard up such that the spell would work. Then adjust the city control, wizard location, and domination and
    // validate each causes the spell to fail.
    Game game = GameTest.createSimpleGame();
    PopCenter city = game.getPopCenter( 0, 0 );
    PopCenter town = new PopCenter( game.generateUniqueId(), "aTown", 0, new Location( 0, 1 ), PopCenter.PopType.Town );
    town.setLevel( 1 );
    city.getLocation().getRegion().addPopCenter( town );
    city.setLevel( 20 );
    Player playerA = game.getPlayers().get( 0 );
    playerA.add( city );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setBase( city );

    assertSame( city.getLocation().getRegion().getControlLevel( playerA ), ControlLevel.Domination );
    wizardForA.setLevel( 9 );
    wizardForA.getOwner().getKingdom().setWizardLevelCap( 10 );
    DarkRitual darkRitualOrder = new DarkRitual( wizardForA );
    darkRitualOrder.execute( game );
    assertTrue( darkRitualOrder.wasExecuted() );
    assertEquals( 1, city.getLevel() );
    assertEquals( 10, wizardForA.getLevel() );

    wizardForA.getOrdersExecuted().clear();
    wizardForA.setBase( town );
    city.setLevel( 20 );
    assertSame( city.getLocation().getRegion().getControlLevel( playerA ), ControlLevel.Domination );
    DarkRitual newDarkRitualOrder = new DarkRitual( wizardForA );
    newDarkRitualOrder.execute( game );
    assertFalse( newDarkRitualOrder.wasExecuted() );

    city.setLevel( 1 );
    wizardForA.getOrdersExecuted().clear();
    assertNotSame( city.getLocation().getRegion().getControlLevel( playerA ), ControlLevel.Domination );
    newDarkRitualOrder = new DarkRitual( wizardForA );
    newDarkRitualOrder.execute( game );
    assertFalse( newDarkRitualOrder.wasExecuted() );

    city.setLevel( 20 );
    wizardForA.getOrdersExecuted().clear();
    wizardForA.setBase( town );
    assertSame( city.getLocation().getRegion().getControlLevel( playerA ), ControlLevel.Domination );
    newDarkRitualOrder = new DarkRitual( wizardForA );
    newDarkRitualOrder.execute( game );
    assertFalse( newDarkRitualOrder.wasExecuted() );
  }
}
