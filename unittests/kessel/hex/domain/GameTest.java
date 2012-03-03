package kessel.hex.domain;

import kessel.hex.map.GameMap;
import kessel.hex.map.GameMapTest;
import kessel.hex.map.MapCreator;
import kessel.hex.map.Region;
import kessel.hex.map.Terrain;
import kessel.hex.orders.Order;
import kessel.hex.orders.diplomat.MoveDiplomat;
import kessel.hex.util.Tuple;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/** Test overall game features. */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class GameTest
{
  private static final Logger LOG = Logger.getLogger( GameTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game oldGame = createSimpleGame();
    StringWriter oldSw = new StringWriter();
    oldGame.save( oldSw );
    LOG.info( oldSw.toString() );

    Game newGame = Game.load( new StringReader( oldSw.toString() ) );
    StringWriter newSw = new StringWriter();
    newGame.save( newSw );
    assertEquals( oldSw.toString(), newSw.toString() );
    assertEquals( oldGame, newGame );
  }

  @Test
  public void testProcessOrders()
  {
    // Create the game with a player that has a couple diplomats and an army.
    Game game = createSimpleGame();
    game.setTurn( 4 );
    PopCenter oldPopCenter = game.getPopCenter( new Tuple( 0, 0 ) );
    Diplomat bob = new Diplomat( game.generateUniqueId(), "Bob", 3, oldPopCenter );
    Diplomat doug = new Diplomat( game.generateUniqueId(), "Doug", 3, oldPopCenter );
    Player playerA = game.getPlayers().get( 0 );
    playerA.add( bob );
    playerA.add( doug );
    Army army = new Army( game.generateUniqueId(), "ArmyA", 3, game.getMap().getLocation( 1, 1 ) );
    army.addUnit( new ArmyUnit( TroopType.LEVY ) );
    playerA.add( army );

    // Move the Diplomat bob to a new pop center.
    PopCenter newPopCenter = game.getPopCenter( new Tuple( 1, 0 ) );
    MoveDiplomat orderBob = new MoveDiplomat( bob, newPopCenter );

    // Move the Diplomat doug to the army.
    MoveDiplomat orderDoug = new MoveDiplomat( doug, army );

    game.processOrders( Arrays.<Order>asList( orderBob, orderDoug ) );
    assertSame( oldPopCenter, bob.getBase() );
    assertSame( army, doug.getBase() );
  }

  @Test
  public void testExtendedPersistence() throws IOException
  {
    // Create the game with a player that has a couple diplomats and an army.
    Game oldGame = createSimpleGame();
    Player playerA = oldGame.getPlayers().get( 0 );
    Player playerB = oldGame.getPlayers().get( 1 );

    // Add things to player A and B
    // .. add pops
    PopCenter popA = oldGame.getPopCenter( new Tuple( 0, 0 ) );
    popA.setOwner( playerA );
    oldGame.addPopCenter( popA );
    PopCenter popB = oldGame.getPopCenter( new Tuple( 1, 0 ) );
    popB.setOwner( playerB );
    oldGame.addPopCenter( popB );

    // .. add diplomats
    Diplomat diplomatAA = new Diplomat( oldGame.generateUniqueId(), "DiplomatAA", 4, popA );
    playerA.add( diplomatAA );
    Diplomat diplomatAB = new Diplomat( oldGame.generateUniqueId(), "DiplomatAB", 4, popB );
    playerA.add( diplomatAB );

    Diplomat diplomatBA = new Diplomat( oldGame.generateUniqueId(), "DiplomatBA", 4, popB );
    playerB.add( diplomatBA );
    Diplomat diplomatBB = new Diplomat( oldGame.generateUniqueId(), "DiplomatBB", 4, popA );
    playerB.add( diplomatBB );

    // .. add things to their views.
    playerA.updateIntelligence( oldGame );
    playerA.addKnownItem( diplomatBB );
    playerB.updateIntelligence( oldGame );
    playerB.addKnownItem( diplomatAB );

    // .. know about some locations.
    playerA.addKnownLocation( popA.getLocation() );
    playerA.addKnownLocation( popB.getLocation() );
    playerB.addKnownLocation( popA.getLocation() );

    // Test persistence for the players.
    StringWriter swA_old = new StringWriter();
    playerA.save( swA_old );
    LOG.info( swA_old.toString() );
    Player playerADeserialized = Player.load( new StringReader( swA_old.toString() ) );
    playerADeserialized.fixDeserializationReferences();
    StringWriter swA_new = new StringWriter();
    playerADeserialized.save( swA_new );
    assertEquals( swA_old.toString(), swA_new.toString() );
    assertEquals( playerA, playerADeserialized );
//    playerA.testEquals( playerADeserialized );

    StringWriter swB_old = new StringWriter();
    playerB.save( swB_old );
    LOG.info( swB_old.toString() );
    Player playerBDeserialized = Player.load( new StringReader( swB_old.toString() ) );
    playerBDeserialized.fixDeserializationReferences();
    StringWriter swB_new = new StringWriter();
    playerBDeserialized.save( swB_new );
    assertEquals( swB_old.toString(), swB_new.toString() );
    assertEquals( playerB, playerBDeserialized );

    assertEquals( 2, oldGame.getMap().getRegions().size() );
    assertEquals( 3, oldGame.getMap().getRegions().get( 0 ).getLocations().size() );
    assertEquals( 1, oldGame.getMap().getRegions().get( 0 ).getPopCenters().size() );

    // Test persistence for the game overall.
    StringWriter oldSW = new StringWriter();
    oldGame.save( oldSW );
    LOG.info( oldSW.toString() );
    Game newGame = Game.load( new StringReader( oldSW.toString() ) );
    StringWriter newSW = new StringWriter();
    newGame.save( newSW );
    assertEquals( oldSW.toString(), newSW.toString() );
    assertEquals( 2, newGame.getMap().getRegions().size() );
    assertEquals( 3, newGame.getMap().getRegions().get( 0 ).getLocations().size() );
    assertEquals( 1, newGame.getMap().getRegions().get( 0 ).getPopCenters().size() );
    assertEquals( oldGame, newGame );
  }

  public static Game createSimpleGame()
  {
    GameMap map = GameMapTest.createSimpleMap( 3, 3 );

    // Create a simple 3x3 map with 2 regions.
    MapCreator creator = new MapCreator( null, null );
    creator.initRegionNames();
    Region regionA = new Region( creator.nextRegionName( Terrain.Plain ) );
    Region regionB = new Region( creator.nextRegionName( Terrain.Forest ) );
    map.getLocations()[0][0].setTerrain( Terrain.Plain );
    map.getLocations()[0][0].setRegion( regionA );
    map.getLocations()[0][1].setTerrain( Terrain.Forest );
    map.getLocations()[0][1].setRegion( regionA );
    map.getLocations()[0][2].setTerrain( Terrain.Forest );
    map.getLocations()[0][2].setRegion( regionA );
    map.getLocations()[1][0].setTerrain( Terrain.Plain );
    map.getLocations()[1][0].setRegion( regionB );
    map.getLocations()[1][1].setTerrain( Terrain.Forest );
    map.getLocations()[1][1].setRegion( regionB );
    map.getLocations()[1][2].setTerrain( Terrain.Forest );
    map.getLocations()[1][2].setRegion( regionB );
    map.getLocations()[2][0].setTerrain( Terrain.Plain );
    map.getLocations()[2][0].setRegion( regionB );
    map.getLocations()[2][1].setTerrain( Terrain.Forest );
    map.getLocations()[2][1].setRegion( regionB );
    map.getLocations()[2][2].setTerrain( Terrain.Forest );
    map.getLocations()[2][2].setRegion( regionB );
    regionA.addLocation( map.getLocations()[0][0] );
    regionA.addLocation( map.getLocations()[0][1] );
    regionA.addLocation( map.getLocations()[0][2] );
    regionB.addLocation( map.getLocations()[1][0] );
    regionB.addLocation( map.getLocations()[1][1] );
    regionB.addLocation( map.getLocations()[1][2] );
    regionB.addLocation( map.getLocations()[2][0] );
    regionB.addLocation( map.getLocations()[2][1] );
    regionB.addLocation( map.getLocations()[2][2] );
    map.addRegion( regionA );
    map.addRegion( regionB );

    // Add 2 players, each with a pop center.
    Game game = new Game( "Test", map );
    PlayerCreator playerCreator = new PlayerCreator( game );
    List<Player> players = playerCreator.createPlayers( 2, false );
    game.setPlayers( players );

    PopCenter popA = new PopCenter( game.generateUniqueId(), "popA", 0, game.getMap().getLocation( 0, 0 ), PopCenter.PopType.City );
    popA.setLevel( 2 );
    game.addPopCenter( popA );
    regionA.addPopCenter( popA );

    PopCenter popB = new PopCenter( game.generateUniqueId(), "popB", 1, game.getMap().getLocation( 1, 0 ), PopCenter.PopType.Hamlet );
    popB.setLevel( 2 );
    game.addPopCenter( popB );
    regionB.addPopCenter( popB );

    PopCenter popC = new PopCenter( game.generateUniqueId(), "popC", 1, game.getMap().getLocation( 1, 1 ), PopCenter.PopType.Town );
    popC.setLevel( 2 );
    game.addPopCenter( popC );
    regionB.addPopCenter( popC );
    popC.setOwner( Player.UNOWNED );

    PopCenter popD = new PopCenter( game.generateUniqueId(), "popD", 0, game.getMap().getLocation( 2, 2 ), PopCenter.PopType.City );
    popD.setLevel( 2 );
    regionB.addPopCenter( popD );
    popD.setOwner( Player.UNOWNED );
    game.addPopCenter( popD );

    players.get( 0 ).add( popA );
    players.get( 0 ).setCapitol( popA );
    players.get( 0 ).getKing().setBase( popA );
    for ( Agent agent : players.get( 0 ).getAgents() )
    {
      agent.setBase( popA );
    }
    for ( Army army : players.get( 0 ).getArmies() )
    {
      army.setLocation( popA.getLocation() );
    }
    for ( Wizard wizard : players.get( 0 ).getWizards() )
    {
      wizard.setBase( popA );
    }
    for ( Diplomat diplomat : players.get( 0 ).getDiplomats() )
    {
      diplomat.setBase( popA );
    }

    players.get( 1 ).add( popB );
    players.get( 1 ).setCapitol( popB );
    players.get( 1 ).getKing().setBase( popB );
    for ( Agent agent : players.get( 1 ).getAgents() )
    {
      agent.setBase( popB );
    }
    for ( Army army : players.get( 1 ).getArmies() )
    {
      army.setLocation( popB.getLocation() );
    }
    for ( Wizard wizard : players.get( 1 ).getWizards() )
    {
      wizard.setBase( popB );
    }
    for ( Diplomat diplomat : players.get( 1 ).getDiplomats() )
    {
      diplomat.setBase( popB );
    }

    players.get( 0 ).setupEmbassies( game.getMap().getRegions() );
    players.get( 1 ).setupEmbassies( game.getMap().getRegions() );
    game.updateIntelligence();
    return game;
  }
}
