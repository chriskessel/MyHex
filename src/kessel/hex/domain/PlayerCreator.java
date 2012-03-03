package kessel.hex.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Create the players for a game. */
public class PlayerCreator
{
  private final Game _game;

  public PlayerCreator( Game game ) { _game = game; }

  public List<Player> createPlayers( int num, boolean randomize )
  {
    List<Kingdom> kingdoms = new ArrayList<>( Kingdoms.KINGDOMS.values() );
    if ( randomize ) { Collections.shuffle( kingdoms ); }
    List<Player> players = new ArrayList<>();
    for ( int playerNum = 0; playerNum < num; playerNum++ )
    {
      Kingdom kingdom = kingdoms.get( playerNum % kingdoms.size() );
      Player player = new Player( kingdom.getName(), kingdom );
      players.add( player );

      // Add the player's starting agents.
      for ( int agentNum = 0; agentNum < kingdom.getStartingAgents().size(); agentNum++ )
      {
        String name = player.nextFigureName();
        Agent agent = new Agent( _game.generateUniqueId(), name, 0, null );
        agent.setLevel( kingdom.getStartingAgents().get( agentNum ) );
        player.add( agent );
      }

      // Add the player's starting wizards.
      for ( int wizardNum = 0; wizardNum < kingdom.getStartingWizards().size(); wizardNum++ )
      {
        String name = player.nextFigureName();
        Wizard wizard = new Wizard( _game.generateUniqueId(), name, 0, null );
        wizard.setLevel( kingdom.getStartingWizards().get( wizardNum ) );
        player.add( wizard );
      }

      // Add the player's starting diplomats.
      for ( int diplomatNum = 0; diplomatNum < kingdom.getStartingDiplomats().size(); diplomatNum++ )
      {
        String name = player.nextFigureName();
        Diplomat diplomat = new Diplomat( _game.generateUniqueId(), name, 0, null );
        diplomat.setLevel( kingdom.getStartingDiplomats().get( diplomatNum ) );
        player.add( diplomat );
      }

      // Add the player's starting armies.
      for ( int armyNum = 0; armyNum < kingdom.getStartingArmies().size(); armyNum++ )
      {
        String name = player.nextArmyName();
        Army army = new Army( _game.generateUniqueId(), name, 0, null );
        for ( int unitCount = 0; unitCount < kingdom.getStartingArmies().get( armyNum ); unitCount++ )
        {
          ArmyUnit armyUnit = kingdom.createArmyUnit( _game.generateUniqueId() );
          army.addUnit( armyUnit );
        }
        player.add( army );
      }

      // Create the king.
      King king = new King( _game.generateUniqueId(), player.nextFigureName(), 0, null, player );
      player.setKing( king );

      // Miscellaneous.
      player.setPower( kingdom.getStartingPower() );
      player.setGold( kingdom.getStartingGold() * Game.BASE_HAMLET_PRODUCTION * Game.GOLD_GRANULARITY );

      // Note - adding embassies has to wait for kingdom setup after the game has been fully created.
    }
    return players;
  }
}
