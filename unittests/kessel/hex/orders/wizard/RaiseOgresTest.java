package kessel.hex.orders.wizard;

import kessel.hex.domain.Army;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.TroopType;
import kessel.hex.domain.Wizard;
import kessel.hex.map.Terrain;
import kessel.hex.orders.AbstractOrderTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

public class RaiseOgresTest extends AbstractOrderTest<SummonOgres>
{
  private static final Logger LOG = Logger.getLogger( RaiseOgresTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    SummonOgres oldOrder = new SummonOgres( wizard );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    SummonOgres newOrder = Game.GSON.fromJson( jsonOrder, SummonOgres.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testNotInArmy()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    wizard.setLevel( 6 );
    SummonOgres order = new SummonOgres( wizard );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testWrongTerrain()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army army = playerA.getArmies().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    wizard.setBase( army );
    wizard.getLocation().setTerrain( Terrain.Plain );
    wizard.setLevel( 6 );
    SummonOgres order = new SummonOgres( wizard );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testRaiseOgres()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army army = playerA.getArmies().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    wizard.setBase( army );
    wizard.getLocation().setTerrain( Terrain.Mountain );
    wizard.setLevel( 6 );
    SummonOgres order = new SummonOgres( wizard );
    int oldSize = army.getUnits().size();
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( oldSize + 1, army.getUnits().size() );
    assertEquals( TroopType.OGRE.getName(), army.getUnits().get( army.getUnits().size() - 1 ).getTroopType().getName() );
  }
}
