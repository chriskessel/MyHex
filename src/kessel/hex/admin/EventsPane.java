package kessel.hex.admin;

import kessel.hex.domain.GameEvent;
import kessel.hex.map.ControlLevel;
import kessel.hex.map.Location;
import kessel.hex.map.Region;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.ArrayList;
import java.util.List;

/** Displays any game events. */
class EventsPane extends JTabbedPane
{
  private final AbstractGameDisplay _gameDisplay;
  private final JList _eventList;
  private final JTextArea _regionalInfoPanel;

  EventsPane( AbstractGameDisplay gameDisplay )
  {
    super();
    _gameDisplay = gameDisplay;
    setBorder( BorderFactory.createEtchedBorder() );

    // Tabbed pane with Game Events and Regional Info
    _eventList = new JList();
    _eventList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    _eventList.setVisibleRowCount( 5 );
    _eventList.addListSelectionListener( createEventSelectionListener() );
    JScrollPane listScroller = new JScrollPane( _eventList );
    addTab( "Game Events", listScroller );
    _regionalInfoPanel = new JTextArea( "foo" );
    addTab( "Regional Info", _regionalInfoPanel );
  }

  public void init()
  {
    if ( _gameDisplay.isLoaded() )
    {
      // Set the list of Game Events.
      List<GameEvent> events = _gameDisplay.getGameEvents();
      List<String> eventDescriptions = new ArrayList<>();
      for ( GameEvent event : events )
      {
        eventDescriptions.add( "Turn " + event.getTurn() + event.getLocation().getCoord() + ": " + event.getDescription() );
      }
      _eventList.setListData( eventDescriptions.toArray() );
      _eventList.validate();
      _eventList.repaint();

      // Describe regional information.
      StringBuilder sb = new StringBuilder();
      for ( Region region : _gameDisplay.getRegions() )
      {
        sb.append( region.getName() + ":\n" );
        sb.append( "  Embassy Levels:" );
        for ( String playerName : _gameDisplay.getPlayerNames() )
        {
          Integer embassyLevel = _gameDisplay.getEmbassyLevel( region, playerName );
          if ( embassyLevel != null )
          {
            sb.append( " " + playerName + "(" + embassyLevel + ")" );
          }
        }
        sb.append( "\n" );
        sb.append( "  Control Levels: " );
        for ( String playerName : _gameDisplay.getPlayerNames() )
        {
          ControlLevel controlLevel = _gameDisplay.getControlLevel( region, playerName );
          if ( controlLevel != null )
          {
            sb.append( playerName + "(" + controlLevel + ")" );
          }
        }
        sb.append( "\n" );
      }
      _regionalInfoPanel.setText( sb.toString() );
    }
  }

  // A listener that warps to the relevant hex when an event is selected.
  private ListSelectionListener createEventSelectionListener()
  {
    return new ListSelectionListener()
    {
      public void valueChanged( ListSelectionEvent e )
      {
        if ( !e.getValueIsAdjusting() )
        {
          int selection = ((JList) e.getSource()).getSelectedIndex();
          if ( selection >= 0 )
          {
            GameEvent event = _gameDisplay.getGameEvents().get( selection );
            Location location = event.getLocation();
            if ( !Location.NOWHERE.equals( location ) )
            {
              _eventList.clearSelection();
              _gameDisplay.setSelectedHex( location.getCoord() );
              _gameDisplay.repaint();
            }
          }
        }
      }
    };
  }
}
