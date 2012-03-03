package kessel.hex.orders.wizard;

import kessel.hex.domain.Army;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.AbstractOrderTest;
import kessel.hex.orders.army.DemandSurrender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

public class DiplomaticAuraTest extends AbstractOrderTest<DiplomaticAura>
{
  private static final Logger LOG = Logger.getLogger( DiplomaticAuraTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Wizard wizardForA = game.getPlayers().get( 0 ).getWizards().get( 0 );

    DiplomaticAura oldOrder = new DiplomaticAura( wizardForA );
    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    DiplomaticAura newOrder = Game.GSON.fromJson( jsonOrder, DiplomaticAura.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testWizardNotInGroup()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setBase( playerA.getCapitol() );
    DiplomaticAura diplomaticAuraOrder = new DiplomaticAura( wizardForA );
    diplomaticAuraOrder.execute( game );
    assertFalse( diplomaticAuraOrder.wasExecuted() );
  }

  @Test
  public void testDiplomaticAuraImpact()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    Army armyForA = playerA.getArmies().get( 0 );
    wizardForA.setBase( armyForA );

    PopCenter pop = game.getPopCenter( 2, 2 );
    armyForA.setLocation( pop.getLocation() );
    pop.setLevel( 3 );

    // Set up for the aura.
    DiplomaticAura diplomaticAuraOrder = new DiplomaticAura( wizardForA );
    diplomaticAuraOrder = Game.GSON.fromJson( Game.GSON.toJson( diplomaticAuraOrder ), DiplomaticAura.class );
    diplomaticAuraOrder.fixDeserializationReferences( game );
    DemandSurrender demandSurrenderOrder = new DemandSurrender( armyForA );
    demandSurrenderOrder = Game.GSON.fromJson( Game.GSON.toJson( demandSurrenderOrder ), DemandSurrender.class );
    demandSurrenderOrder.fixDeserializationReferences( game );

    // Validate the aura impact.
    assertEquals( 2, demandSurrenderOrder.deriveArmyDiplomaticLevel( game ) );
    diplomaticAuraOrder.execute( game );
    assertEquals( 6, demandSurrenderOrder.deriveArmyDiplomaticLevel( game ) );
  }
}
