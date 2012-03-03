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

/** Rather than repeat them here, trust SubvertCityTest takes care of generic validation, like testing the target is in the same region. */
public class SubvertCityTest extends AbstractSubvertPopCenterTest<SubvertCity>
{
  private static final Logger LOG = Logger.getLogger( SubvertCityTest.class );

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
    SubvertCity oldOrder = new SubvertCity( wizard, pop );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    SubvertCity newOrder = Game.GSON.fromJson( jsonOrder, SubvertCity.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testSuccessOnHamlet()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setBase( game.getPopCenter( new Tuple( 2, 2 ) ) ); // put the wizard in the same region as the hamlet
    playerB.setCapitol( game.getPopCenter( new Tuple( 2, 2 ) ) ); // playerB must stop using the hamlet as the capitol.
    PopCenter hamlet = game.getPopCenter( new Tuple( 1, 0 ) );
    assertEquals( PopCenter.PopType.Hamlet, hamlet.getType() );
    wizardForA.setLevel( 8 );

    SubvertCity order = new SubvertCity( wizardForA, hamlet );
    assertTrue( playerB.getPopCenters().contains( hamlet ) );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( playerA, hamlet.getOwner() );
    assertFalse( playerB.getPopCenters().contains( hamlet ) );
  }

  @Test
  public void testSuccessOnTown()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setBase( game.getPopCenter( new Tuple( 2, 2 ) ) ); // put the wizard in the same region as the town
    PopCenter town = game.getPopCenter( new Tuple( 1, 1 ) );
    assertEquals( PopCenter.PopType.Town, town.getType() );
    wizardForA.setLevel( 8 );

    SubvertCity order = new SubvertCity( wizardForA, town );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( playerA, town.getOwner() );
  }

  @Test
  public void testSuccessOnCity()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setBase( game.getPopCenter( new Tuple( 1, 1 ) ) ); // put the wizard in the same region as the city
    PopCenter city = game.getPopCenter( new Tuple( 2, 2 ) );
    assertEquals( PopCenter.PopType.City, city.getType() );
    wizardForA.setLevel( 8 );

    SubvertCity order = new SubvertCity( wizardForA, city );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( playerA, city.getOwner() );
  }
}
