package kessel.hex.admin;

import kessel.hex.domain.Game;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/** Handle operational functions for the game: - creating the game - running turns - ??? */
@SuppressWarnings({ "ResultOfMethodCallIgnored" })
public class GameManager
{
  private static final Logger LOG = Logger.getLogger( GameManager.class );

  public static final String CREATE = "create";
  public static final String RUN_TURN = "run_turn";
  public static final String SAVES_DIR = "saves";
  public static final String CONF_DIR = "conf";

  public static void main( String[] args ) throws IOException
  {
    if ( args.length == 0 || args.length > 2 )
    {
      printUsage();
    }
    else if ( args[0].equals( CREATE ) )
    {
      createGame( args[1] );
    }
    else if ( args[0].equals( RUN_TURN ) )
    {
      runTurn( args[1] );
    }
    else
    {
      printUsage();
    }
  }

  private static void createGame( String name ) throws IOException
  {
    // TODO - pass in on command line?
    int numPlayers = 3;
    int hexesPerPlayer = 50;
    double regionsPerPlayer = 0.67;
    int townsPerPlayer = 8;

    // Create the game and save it in a directory based on it's name.
    Game game = new Game( name );
    File gameDir = new File( SAVES_DIR, name );
    backupPriorGame( gameDir );
    gameDir.mkdir();
    game.createGame( numPlayers, hexesPerPlayer, regionsPerPlayer, townsPerPlayer );
    game.save( gameDir );
  }

  private static void runTurn( String name ) throws IOException
  {
    File gameDir = new File( SAVES_DIR, name );
    File oldGameDir = backupPriorGame( gameDir );
    try
    {
      Game game = Game.runTurn( oldGameDir );
      gameDir.mkdir();
      game.save( gameDir );
    }
    catch ( Exception e )
    {
      LOG.error( "Game turn not executed.", e );
      oldGameDir.renameTo( gameDir );
    }
  }

  /** @return the file pointing at the backup directory or null if there was no game to backup. */
  private static File backupPriorGame( File gameDir )
  {
    if ( gameDir.exists() )
    {
      File oldGameDir = new File( SAVES_DIR, gameDir.getName() + System.currentTimeMillis() );
      gameDir.renameTo( oldGameDir );
      return oldGameDir;
    }
    else
    {
      return null;
    }
  }

  @SuppressWarnings({ "UseOfSystemOutOrSystemErr" })
  private static void printUsage()
  {
    System.out.println( "Usage: GameManager " + CREATE + " <game name> | " + RUN_TURN + " <game name>" );
  }
}