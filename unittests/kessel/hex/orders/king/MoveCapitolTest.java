package kessel.hex.orders.king;

import kessel.hex.domain.Army;
import kessel.hex.domain.Figure;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.AbstractOrderTest;
import kessel.hex.orders.wizard.TrainWizard;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class MoveCapitolTest extends AbstractOrderTest<MoveCapitol>
{
  private static final Logger LOG = Logger.getLogger( MoveCapitolTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( MoveCapitol a, MoveCapitol b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a._target.getId(), b._jsonTargetId.intValue() );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    PopCenter pop = game.getPopCenter( 1, 1 );
    playerA.add( pop );
    assertFalse( pop.equals( playerA.getCapitol() ) );
    MoveCapitol oldOrder = new MoveCapitol( playerA.getKing(), pop );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    MoveCapitol newOrder = Game.GSON.fromJson( jsonOrder, MoveCapitol.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testNotEnoughMoney()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    PopCenter pop = game.getPopCenter( 1, 1 );
    playerA.add( pop );
    playerA.setGold( 0 );
    assertFalse( pop.equals( playerA.getCapitol() ) );
    MoveCapitol order = new MoveCapitol( playerA.getKing(), pop );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testNotMyPop()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    playerA.setGold( 40000 );
    PopCenter pop = game.getPopCenter( 1, 1 );
    playerA.add( pop );
    playerA.setGold( 0 );
    assertFalse( pop.equals( playerA.getCapitol() ) );
    MoveCapitol order = new MoveCapitol( playerA.getKing(), pop );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testMoved()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    PopCenter pop = game.getPopCenter( 1, 1 );
    playerA.add( pop );
    playerA.setGold( 40000 );

    // Make someone at the capitol busy before the move.
    List<Figure> figuresAtCapitol = new ArrayList<>();
    for ( Figure figure : playerA.getFigures() )
    {
      if ( figure.getBase().equals( playerA.getCapitol() ) ) { figuresAtCapitol.add( figure ); }
    }
    Wizard wizIsBusy = (Wizard) figuresAtCapitol.remove( 0 );
    wizIsBusy.addOrderExecuted( new TrainWizard( wizIsBusy ) );

    // Execute the capitol move.
    assertFalse( pop.equals( playerA.getCapitol() ) );
    MoveCapitol order = new MoveCapitol( playerA.getKing(), pop );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( pop, playerA.getCapitol() );

    // Verify all the figures moved with the king, except the one that was already busy.
    assertEquals( playerA.getKing().getBase(), pop );
    assertFalse( playerA.getCapitol().equals( wizIsBusy.getBase() ) );
    for ( Figure figure : figuresAtCapitol )
    {
      assertEquals( playerA.getCapitol(), figure.getBase() );
    }

    // Verify the inactive army moved.
    List<Army> inactiveArmies = new ArrayList<>( playerA.getArmies() );
    inactiveArmies.removeAll( playerA.getActiveArmies() );
    assertEquals( 1, inactiveArmies.size() );
    assertEquals( inactiveArmies.get( 0 ).getLocation().getCoord(), playerA.getCapitol().getLocation().getCoord() );
  }
}
