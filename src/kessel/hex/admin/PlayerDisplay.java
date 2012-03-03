package kessel.hex.admin;

import kessel.hex.domain.Game;
import kessel.hex.domain.GameEvent;
import kessel.hex.domain.Player;
import kessel.hex.map.ControlLevel;
import kessel.hex.map.Region;
import kessel.hex.util.Tuple;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Show info for a specific player. */
@SuppressWarnings({ "ObjectEquality" })
public class PlayerDisplay extends AbstractGameDisplay
{
  Player _player;

  public static void main( String[] args ) throws Exception
  {
    PlayerDisplay playerDisplay = new PlayerDisplay();
    playerDisplay.setLocation( 1200, 500 );
    playerDisplay.init();
  }

  protected void addMenuBar()
  {
    JMenuBar menuBar = new JMenuBar();
    JMenu menu = new JMenu( "File" );
    JMenuItem saveItem = new JMenuItem( createSaveAction() );
    JMenuItem loadItem = new JMenuItem( createLoadAction() );
    menu.add( saveItem );
    menu.add( loadItem );
    menuBar.add( menu );
    setJMenuBar( menuBar );
  }

  protected Action createSaveAction()
  {
    return new AbstractAction( "Save" )
    {
      public void actionPerformed( ActionEvent event )
      {
//                try
//                {
//// CODEREVIEW                    _player.saveOrders( getPersistenceDir(), _player.getTurn() + 1 );
//                }
//                catch ( IOException e )
//                {
//                    throw new RuntimeException( e );
//                }
      }
    };
  }

  protected Action createLoadAction()
  {
    return new AbstractAction( "Load" )
    {
      public void actionPerformed( ActionEvent event )
      {
        final JFileChooser fc = new JFileChooser();
        fc.setFileFilter( new FileFilter()
        {
          public boolean accept( File pathName )
          {
            return pathName.getName().endsWith( Player.STATE_FILE );
          }

          public String getDescription() { return "Player state files"; }
        } );
        fc.setCurrentDirectory( getPersistenceDir() );
        int returnVal = fc.showOpenDialog( (Component) event.getSource() );
        if ( returnVal == JFileChooser.APPROVE_OPTION )
        {
          File file = fc.getSelectedFile();
          String[] fileBits = file.getName().split( "_" );
          String name = fileBits[0];
          String turn = fileBits[1].substring( 4 );
          try
          {
            _player = Player.load( getPersistenceDir(), name, Integer.parseInt( turn ) );
          }
          catch ( Exception e )
          {
            throw new RuntimeException( e );
          }
          _statusPane.setPreferredSize(
            new Dimension( _mapPane.getPreferredSize().width, _statusPane.getPreferredSize().height ) );
          init();
          PlayerDisplay.this.validate();
          PlayerDisplay.this.repaint();
        }
      }
    };
  }

  protected AbstractMapPane createMapPane()
  {
    PlayerMapPane pane = new PlayerMapPane( this, this );
    return pane;
  }

  protected AbstractDetailsPane createDetailsPane()
  {
    return new PlayerDetailsPane( this );
  }

  public void init()
  {
    super.init();
  }

  private File getPersistenceDir() { return new File( new File( GameManager.SAVES_DIR, "foogame" ), Game.PLAYERS_DIR ); }

  protected int getMapHeight()
  {
    return isLoaded() ? _player.getGameView().getMap().getHeight() : 0;
  }

  protected int getMapWidth()
  {
    return isLoaded() ? _player.getGameView().getMap().getWidth() : 0;
  }

  protected boolean isOnMap( Tuple coord )
  {
    return _player.getGameView().getMap().isOnMap( coord );
  }

  public List<GameEvent> getGameEvents()
  {
    return isLoaded() ? _player.getGameEvents() : Collections.<GameEvent>emptyList();
  }

  public boolean isLoaded()
  {
    return _player != null;
  }

  public List<Region> getRegions()
  {
    List<Region> regions = new ArrayList<>();
// CODEREVIEW       for ( PlayerView.RegionView regionView : _player.getGameView().getRegionViews() )
//        {
//            regions.add( new Region( regionView.getName() ) );
//        }
    return regions;
  }

  public Integer getEmbassyLevel( Region region, String playerName )
  {
    if ( playerName.equals( _player.getName() ) )
    {
      return _player.getEmbassyLevel( region );
    }
    else
    {
// CODEREVIEW           PlayerView.RegionView regionView = _player.getGameView().getRegionView( region.getName() );
//            Map<String, Integer> knownEmbassyLevels = regionView.getKnownEmbassyLevels();
//            return knownEmbassyLevels.get( playerName );
      return 0;
    }
  }

  public ControlLevel getControlLevel( Region region, String playerName )
  {
// CODEREVIEW       PlayerView.RegionView regionView = _player.getGameView().getRegionView( region.getName() );
//        Map<String, ControlLevel> knownControlLevels = regionView.getKnownControlLevels();
//        return knownControlLevels.get( playerName );
    return null;
  }

  public List<String> getPlayerNames()
  {
    return _player.getGameView().getPlayerNames();
  }
}