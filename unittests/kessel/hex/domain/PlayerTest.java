package kessel.hex.domain;

import kessel.hex.orders.diplomat.MoveDiplomat;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class PlayerTest
{
  private static final Logger LOG = Logger.getLogger( PlayerTest.class );

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
    playerA.updateIntelligence( game );
    playerB.updateIntelligence( game );

    // Add some stuff for the playerA view to know about.
    PopCenter popA = game.getPopCenter( 2, 2 );
    Diplomat diplomatA = playerA.getDiplomats().get( 0 );
    playerA.addKnownItem( game.getPopCenter( 1, 1 ) );
    playerA.addKnownItem( game.getPopCenter( 2, 2 ) );
    playerA.addKnownItem( playerB.getDiplomats().get( 0 ) );
    playerA.addKnownItem( playerB.getDiplomats().get( 0 ).getBase() );
    playerA.addKnownLocation( game.getMap().getLocation( 2, 2 ) );

    // Add some orders
    PopCenter popB = game.getPopCenter( 1, 1 );
    MoveDiplomat order = new MoveDiplomat( diplomatA, popB );
    playerA.addOrder( order );
    MoveDiplomat orderTwo = new MoveDiplomat( diplomatA, popA );
    playerA.addOrder( orderTwo );

    // Test persistence for the player state.
    StringWriter old_sw = new StringWriter();
    game.updateIntelligence();
    playerA.save( old_sw );
    LOG.debug( old_sw.toString() );
    Player playerADeserialized = Player.load( new StringReader( old_sw.toString() ) );
    playerADeserialized.fixDeserializationReferences();
    StringWriter new_sw = new StringWriter();
    playerADeserialized.save( new_sw );
    assertEquals( old_sw.toString(), new_sw.toString() );
    playerA.spewEquals( playerADeserialized );
    assertEquals( 27, playerA.getKnownItems().size() );

    // Test persistence for the player orders.
    assertEquals( 2, playerADeserialized.getNextTurnOrders().size() );
  }

  @Test
  public void testForcedCapitolRelocation() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    playerB.add( game.getPopCenter( 1, 1 ) );

    // Verify a capitol moved to the wild. It'll lose any inactive armies.
    assertEquals( playerA.getActiveArmies().size(), playerA.getArmies().size() - 1 );
    playerA.forcedCapitolRelocation();
    assertEquals( 9, playerA.getPower() );
    assertEquals( PopCenter.THE_WILDS, playerA.getCapitol() );
    assertEquals( PopCenter.THE_WILDS, playerA.getKing().getBase() );
    assertEquals( playerA.getActiveArmies().size(), playerA.getArmies().size() );

    // Verify a capitol moved to another pop. The inactive army goes with it.
    assertEquals( playerB.getActiveArmies().size(), playerB.getArmies().size() - 1 );
    playerB.forcedCapitolRelocation();
    assertEquals( game.getPopCenter( 1, 1 ), playerB.getCapitol() );
    assertEquals( playerB.getKing().getBase(), playerB.getCapitol() );
    assertEquals( playerB.getActiveArmies().size(), playerB.getArmies().size() - 1 );
    assertFalse( playerB.getArmies().get( 2 ).isActive() );
    assertEquals( playerB.getArmies().get( 2 ).getLocation(), playerB.getCapitol().getLocation() );
  }

  @Test // Not really a test per se, just exercising the updatePlayerIntel code and spewing the player's json file.
  public void testIntelUpdates() throws IOException
  {
    Game game = GameTest.createSimpleGame();
    Player player = game.getPlayers().get( 0 );
    for ( int i = 0; i < 50; i++ ) // just for fun, see regional group reporting work.
    {
      player.getArmies().get( 0 ).addUnit( new ArmyUnit( TroopType.LEVY ) );
    }
    game.updateIntelligence();
    StringWriter sw = new StringWriter();
    game.getPlayers().get( 1 ).save( sw );
    assertEquals( 22, game.getPlayers().get( 1 ).getKnownItems().size() );
    LOG.debug( sw.toString() );
  }
}
