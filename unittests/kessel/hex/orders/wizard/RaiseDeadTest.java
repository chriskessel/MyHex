package kessel.hex.orders.wizard;

import kessel.hex.domain.Army;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.TroopType;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.AbstractOrderTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

public class RaiseDeadTest extends AbstractOrderTest<RaiseDead>
{
  private static final Logger LOG = Logger.getLogger( RaiseDeadTest.class );

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
    RaiseDead oldOrder = new RaiseDead( wizard );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    RaiseDead newOrder = Game.GSON.fromJson( jsonOrder, RaiseDead.class);
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testNotInArmy()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    RaiseDead order = new RaiseDead( wizard );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testRaiseDead()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army army = playerA.getArmies().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    wizard.setBase( army );
    wizard.setLevel( 4 );
    RaiseDead order = new RaiseDead( wizard );
    int oldSize = army.getUnits().size();
    double oldSupport = army.getSupportRequired();
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( oldSize + 1, army.getUnits().size() );
    assertEquals( TroopType.UNDEAD.getName(), army.getUnits().get( army.getUnits().size() - 1 ).getTroopType().getName() );
    assertEquals( oldSupport, army.getSupportRequired(), 0.01 );
  }
}
