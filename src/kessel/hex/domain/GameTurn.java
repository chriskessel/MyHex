package kessel.hex.domain;

import kessel.hex.map.Region;
import kessel.hex.orders.Order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Holds information specific to a given game turn. */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class GameTurn
{
  /** A map of orders executed for the turn, keyed by the order class name. */
  private final Map<String, List<? extends Order>> _ordersByType = new HashMap<>();
  private Map<Region, Player> _regionalControlInfo = new HashMap<>();

  public <T> List<T> getOrdersOfType( Class<T> clazz )
  {
    List<? extends Order> orders = _ordersByType.get( clazz.getName() );
    if ( orders == null )
    {
      orders = Collections.emptyList();
    }
    return (List<T>) orders;
  }

  public <T> void addOrderExecuted( T order )
  {
    List<T> orders = (List<T>) _ordersByType.get( order.getClass().getName() );
    if ( orders == null )
    {
      orders = new ArrayList<>();
      _ordersByType.put( order.getClass().getName(), (List<? extends Order>) orders );
    }
    orders.add( order );
  }

  public void setRegionalControlInfo( Map<Region, Player> regionalControlInfo ) { _regionalControlInfo = regionalControlInfo; }

  public Map<Region, Player> getRegionalControlInfo() { return _regionalControlInfo; }
}
