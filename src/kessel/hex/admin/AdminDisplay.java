package kessel.hex.admin;

import kessel.hex.domain.Game;
import kessel.hex.domain.GameEvent;
import kessel.hex.domain.Player;
import kessel.hex.map.ControlLevel;
import kessel.hex.map.Region;
import kessel.hex.util.Tuple;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Show the Game. */
@SuppressWarnings({ "ObjectEquality" })
public class AdminDisplay extends AbstractGameDisplay
{
  Game _game;

  public static void main( String[] args )
  {
    AdminDisplay gameDisplay = new AdminDisplay();
    gameDisplay.setLocation( 1200, 500 );
    gameDisplay.init();
  }

  protected void addMenuBar()
  {
  }

  protected AbstractMapPane createMapPane()
  {
    return new AdminMapPane( this, this );
  }

  protected AbstractDetailsPane createDetailsPane()
  {
    return new AdminDetailsPane( this );
  }

  public void init()
  {
    // Decide if we're loading an old game or new game.
    File gameDir = new File( GameManager.SAVES_DIR, "foogame" );
    File gameFile = new File( new File( gameDir, Game.ADMIN_DIR ), Game.GAME_FILE );
    if ( gameFile.exists() )
    {
      _game = Game.load( gameDir );
    }
    else
    {
      createGame();
    }
    _statusPane.setPreferredSize(
      new Dimension( _mapPane.getPreferredSize().width, _statusPane.getPreferredSize().height ) );
    super.init();
    invalidate();
    repaint();
  }

  protected int getMapHeight() { return _game.getMap().getHeight(); }

  protected int getMapWidth() { return _game.getMap().getWidth(); }

  private void createGame()
  {
    final int numPlayers = 3;
    final int hexesPerPlayer = 60;
    final double regionsPerPlayer = 0.67;
    final int townsPerPlayer = 8;
    int size = (int) Math.sqrt( hexesPerPlayer * numPlayers );
    setSize( CELL_RADIUS * 2 * size, CELL_RADIUS * 2 * size );
    _game = new Game( "Test" );

    _game.setCreationListener( _mapPane );
    new Thread( new Runnable()
    {
      public void run()
      {
        _game.createGame( numPlayers, hexesPerPlayer, regionsPerPlayer, townsPerPlayer );
      }
    } ).start();
  }

  protected boolean isOnMap( Tuple coord )
  {
    return _game.getMap().isOnMap( coord );
  }

  public List<GameEvent> getGameEvents()
  {
    return Collections.emptyList(); // TODO - Gather up all player events?
  }

  public boolean isLoaded()
  {
    return _game != null;
  }

  public List<Region> getRegions()
  {
    return _game.getMap().getRegions();
  }

  public Integer getEmbassyLevel( Region region, String playerName )
  {
    return _game.getEmbassyLevel( region, playerName );
  }

  public ControlLevel getControlLevel( Region region, String playerName )
  {
    return _game.getControlLevel( region, playerName );
  }

  public List<String> getPlayerNames()
  {
    List<String> names = new ArrayList<>();
    for ( Player player : _game.getPlayers() )
    {
      names.add( player.getName() );
    }
    return names;
  }
}