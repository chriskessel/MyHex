package kessel.hex.orders.army;

import kessel.hex.domain.Army;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.TroopType;
import kessel.hex.orders.AbstractOrderTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test army recruiting. */
public class ArmyRecruitTest extends AbstractOrderTest<ArmyRecruit>
{
  private static final Logger LOG = Logger.getLogger( ArmyRecruitTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army army = playerA.getArmies().get( 0 );
    ArmyRecruit oldOrder = new ArmyRecruit( army );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    ArmyRecruit newOrder = Game.GSON.fromJson( jsonOrder, ArmyRecruit.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testNotMyPop()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army army = playerA.getArmies().get( 0 );
    army.setLocation( playerB.getCapitol().getLocation() );
    ArmyRecruit order = new ArmyRecruit( army );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testPopIsHamlet()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army army = playerA.getArmies().get( 0 );
    PopCenter hamlet = game.getPopCenter( 1, 0 );
    army.setLocation( hamlet.getLocation() );
    playerA.add( hamlet );
    ArmyRecruit order = new ArmyRecruit( army );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testOutOfMoney()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army army = playerA.getArmies().get( 0 );
    playerA.setGold( 1 );
    ArmyRecruit order = new ArmyRecruit( army );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testOverRecruited()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army army = playerA.getArmies().get( 0 );
    playerA.setGold( 10000 );
    PopCenter pop = game.getPopCenter( army.getLocation() );
    pop.addOrderExecuted( new ArmyRecruit( army ) );
    pop.addOrderExecuted( new ArmyRecruit( army ) );
    ArmyRecruit order = new ArmyRecruit( army );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testRecruitTownMilitia()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    addBunchOfPopsSoPlayerHasEnoughSupport( game, playerA );
    Army army = playerA.getArmies().get( 0 );
    playerA.setGold( 10000 );
    army.getUnits().clear();
    ArmyRecruit order = new ArmyRecruit( army );
    PopCenter pop = game.getPopCenter( 1, 1 );
    playerA.add( pop );
    army.setLocation( pop.getLocation() );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 1, army.getUnits().size() );
    assertEquals( TroopType.LEVY.getName(), army.getUnits().get( 0 ).getTroopType().getName() );
  }

  private void addBunchOfPopsSoPlayerHasEnoughSupport( Game game, Player playerA )
  {
    playerA.add( new PopCenter( game.generateUniqueId(), "foo", 0, playerA.getCapitol().getLocation(), PopCenter.PopType.Hamlet ) );
    playerA.add( new PopCenter( game.generateUniqueId(), "foo", 0, playerA.getCapitol().getLocation(), PopCenter.PopType.Hamlet ) );
    playerA.add( new PopCenter( game.generateUniqueId(), "foo", 0, playerA.getCapitol().getLocation(), PopCenter.PopType.Hamlet ) );
    playerA.add( new PopCenter( game.generateUniqueId(), "foo", 0, playerA.getCapitol().getLocation(), PopCenter.PopType.Hamlet ) );
  }

  @Test
  public void testRecruitCityMilitia()
  {
    Game game = GameTest.createSimpleGame();
    PopCenter newPop = new PopCenter( game.generateUniqueId(), "foo", 0, game.getMap().getLocation( 0, 1 ), PopCenter.PopType.Town );
    newPop.setLevel( 99 );
    game.addPopCenter( newPop );
    game.getMap().getRegions().get( 0 ).addPopCenter( newPop );
    Player playerA = game.getPlayers().get( 0 );
    addBunchOfPopsSoPlayerHasEnoughSupport( game, playerA );
    Army army = playerA.getArmies().get( 0 );
    playerA.setGold( 10000 );
    army.getUnits().clear();
    ArmyRecruit order = new ArmyRecruit( army );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 1, army.getUnits().size() );
    assertEquals( TroopType.LEVY.getName(), army.getUnits().get( 0 ).getTroopType().getName() );
  }

  @Test
  public void testRecruitCityRegional()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    addBunchOfPopsSoPlayerHasEnoughSupport( game, playerA );
    Army army = playerA.getArmies().get( 0 );
    playerA.setGold( 10000 );
    army.getUnits().clear();
    ArmyRecruit order = new ArmyRecruit( army );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 1, army.getUnits().size() );
    assertEquals( TroopType.REGIONAL.getName(), army.getUnits().get( 0 ).getTroopType().getName() );
  }

  @Test
  public void testRecruitTwice()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    addBunchOfPopsSoPlayerHasEnoughSupport( game, playerA );
    Army army = playerA.getArmies().get( 0 );
    playerA.setGold( 10000 );
    army.getUnits().clear();
    ArmyRecruit orderOne = new ArmyRecruit( army );
    ArmyRecruit orderTwo = new ArmyRecruit( army );
    assertEquals( 2, game.getPopCenter( army.getLocation() ).getLevel() );
    orderOne.execute( game );
    assertEquals( 2, game.getPopCenter( army.getLocation() ).getLevel() );
    orderTwo.execute( game );
    assertTrue( orderOne.wasExecuted() );
    assertTrue( orderTwo.wasExecuted() );
    assertEquals( 2, army.getUnits().size() );
    assertEquals( TroopType.REGIONAL.getName(), army.getUnits().get( 0 ).getTroopType().getName() );
    assertEquals( TroopType.REGIONAL.getName(), army.getUnits().get( 1 ).getTroopType().getName() );
    assertEquals( 1, game.getPopCenter( army.getLocation() ).getLevel() );
  }

  @Test
  public void testRecruitCapitolKingdomTroop()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    addBunchOfPopsSoPlayerHasEnoughSupport( game, playerA );
    Army army = playerA.getArmies().get( 0 );
    army.setLocation( playerA.getCapitol().getLocation() );
    playerA.setGold( 0 ); // no gold required for kingdom troops.
    playerA.setKingdomTroopsAvailable( 1 );
    army.getUnits().clear();
    ArmyRecruit order = new ArmyRecruit( army );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 1, army.getUnits().size() );
    assertEquals( playerA.getKingdom().getTroopType().getName(), army.getUnits().get( 0 ).getTroopType().getName() );
  }

  @Test
  public void testRecruitCapitolRegionalTroop()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    addBunchOfPopsSoPlayerHasEnoughSupport( game, playerA );
    Army army = playerA.getArmies().get( 0 );
    army.setLocation( playerA.getCapitol().getLocation() );
    playerA.setGold( 10000 );
    playerA.setKingdomTroopsAvailable( 0 );
    army.getUnits().clear();
    ArmyRecruit order = new ArmyRecruit( army );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 1, army.getUnits().size() );
    assertEquals( TroopType.REGIONAL.getName(), army.getUnits().get( 0 ).getTroopType().getName() );
  }
}
