package kessel.hex.orders.army;

import kessel.hex.domain.Army;
import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.map.Region;
import kessel.hex.orders.AbstractOrderTest;
import kessel.hex.orders.diplomat.DiplomatInspireLoyalty;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

@SuppressWarnings({ "AssignmentToStaticFieldFromInstanceMethod" })
public class DemandSurrenderTest extends AbstractOrderTest<DemandSurrender>
{
  private static final Logger LOG = Logger.getLogger( DemandSurrenderTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army armyForA = playerA.getArmies().get( 0 );

    DemandSurrender oldOrder = new DemandSurrender( armyForA );
    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    DemandSurrender newOrder = Game.GSON.fromJson( jsonOrder, DemandSurrender.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testNoPopAtLocation()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army armyForA = playerA.getArmies().get( 0 );
    armyForA.setLocation( game.getMap().getLocation( 1, 2 ) );

    DemandSurrender order = new DemandSurrender( armyForA );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), DemandSurrender.class );
    order.fixDeserializationReferences( game );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testAttackerTooSmall()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    armyForA.setLocation( playerB.getCapitol().getLocation() );
    armyForA.getUnits().clear();

    DemandSurrender order = new DemandSurrender( armyForA );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), DemandSurrender.class );
    order.fixDeserializationReferences( game );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }


  @Test
  public void testDiplomacyPopButArmyRetreated()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    armyForA.adjustMorale( 50 );
    armyForA.setLocation( playerA.getCapitol().getLocation() );
    Army armyForB = playerB.getArmies().get( 0 );
    armyForB.getUnits().remove( 0 );
    armyForB.setLocation( playerA.getCapitol().getLocation() );
    PopCenter pop = game.getPopCenter( 2, 2 );
    pop.setLevel( 1 );

    AbstractArmyAttack a_attack_b = new ArmyAttackArmy( armyForA, armyForB );
    a_attack_b = Game.GSON.fromJson( Game.GSON.toJson( a_attack_b ), ArmyAttackArmy.class );
    a_attack_b.fixDeserializationReferences( game );
    a_attack_b.execute( game );
    DemandSurrender b_attack_pop = new DemandSurrender( armyForB );
    b_attack_pop = Game.GSON.fromJson( Game.GSON.toJson( b_attack_pop ), DemandSurrender.class );
    b_attack_pop.fixDeserializationReferences( game );
    b_attack_pop.execute( game );

    // Verify the pop couldn't be attacked due to the army's retreat.
    assertTrue( a_attack_b.wasExecuted() );
    assertTrue( a_attack_b.attackerWon() );
    assertFalse( b_attack_pop.wasExecuted() );
  }

  @Test
  public void testPopIsSelfOwned()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army armyForA = playerA.getArmies().get( 0 );
    PopCenter pop = game.getPopCenter( 2, 2 );
    playerA.add( pop );
    armyForA.setLocation( game.getMap().getLocation( 2, 2 ) );

    DemandSurrender order = new DemandSurrender( armyForA );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), DemandSurrender.class );
    order.fixDeserializationReferences( game );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testInspiresLoyaltyImpact()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );

    // First, create the inspire loyalty order.
    Diplomat diplomatForB = playerB.getDiplomats().get( 0 );
    PopCenter popForB = game.getPopCenter( 1, 1 );
    playerB.add( popForB );
    popForB.setLevel( 5 );
    diplomatForB.setLevel( 3 );
    diplomatForB.setBase( popForB );
    DiplomatInspireLoyalty loyaltyOrder = new DiplomatInspireLoyalty( diplomatForB );

    // Verify the surrender is impacted by the inspire loyalty.
    Army armyForA = playerA.getActiveArmies().get( 0 );
    armyForA.setLocation( popForB.getLocation() );
    DemandSurrender order = new DemandSurrender( armyForA );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), DemandSurrender.class );
    order.fixDeserializationReferences( game );
    assertEquals( 10, order.determinePopResistance( game ) );

    loyaltyOrder.execute( game );
    assertTrue( loyaltyOrder.wasExecuted() );

    assertEquals( 13, order.determinePopResistance( game ) );
  }

  @Test
  public void testEmbassyImpact()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army armyForA = playerA.getActiveArmies().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    PopCenter popForB = game.getPopCenter( 1, 1 );
    playerB.add( popForB );
    popForB.setLevel( 2 );
    Region region = game.getMap().getLocation( 1, 1 ).getRegion();

    // The default resistance.
    armyForA.setLocation( popForB.getLocation() );
    DemandSurrender order = new DemandSurrender( armyForA );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), DemandSurrender.class );
    order.fixDeserializationReferences( game );
    assertEquals( 4, order.determinePopResistance( game ) );

    // Give playerA an embassy, see things got easier.
    playerA.improveEmbassy( region );
    assertEquals( 2, order.determinePopResistance( game ) );

    // Give playerB an embassy, see things got harder.
    playerB.improveEmbassy( region );
    playerB.improveEmbassy( region );
    assertEquals( 6, order.determinePopResistance( game ) );

    // Make the pop unowned, see it got easier again.
    playerB.remove( popForB );
    popForB.setOwner( Player.UNOWNED );
    assertEquals( 1, order.determinePopResistance( game ) );
  }

  @Test
  public void testMilitaryDiplomacyPopFailsOnCapitol()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyForA = playerA.getArmies().get( 0 );
    PopCenter pop = playerB.getCapitol();
    armyForA.setLocation( pop.getLocation() );
    pop.setLevel( 1 );

    DemandSurrender order = new DemandSurrender( armyForA );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), DemandSurrender.class );
    order.fixDeserializationReferences( game );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertFalse( playerA.getPopCenters().contains( pop ) );
  }

  @Test
  public void testMilitaryDiplomacyPopFails()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army armyForA = playerA.getArmies().get( 0 );
    PopCenter pop = game.getPopCenter( 2, 2 );
    armyForA.setLocation( pop.getLocation() );
    pop.setLevel( 99 );

    DemandSurrender order = new DemandSurrender( armyForA );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), DemandSurrender.class );
    order.fixDeserializationReferences( game );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertFalse( playerA.getPopCenters().contains( pop ) );
  }

  @Test
  public void testSuccess()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army armyForA = playerA.getActiveArmies().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    PopCenter popForB = game.getPopCenter( 1, 1 );
    armyForA.setLocation( popForB.getLocation() );
    playerB.add( popForB );
    popForB.setLevel( 1 );

    DemandSurrender order = new DemandSurrender( armyForA );
    int oldPopCountForA = playerA.getPopCenters().size();
    int oldPopCountForB = playerB.getPopCenters().size();
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), DemandSurrender.class );
    order.fixDeserializationReferences( game );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertTrue( order.wasSuccessful() );
    assertEquals( oldPopCountForA + 1, playerA.getPopCenters().size() );
    assertEquals( oldPopCountForB - 1, playerB.getPopCenters().size() );
  }
}
