/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jhotdraw.draw.Drawing;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.components.drawing.course.DrawingMethod;
import org.opentcs.guing.exchange.EventDispatcher;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.LayoutModel;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.opentcs.guing.model.elements.OtherGraphicalElement;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.StaticRouteModel;
import org.opentcs.guing.model.elements.VehicleModel;

/**
 * Interface f�r das Datenmodell des gesamten modellierten Systems. Besteht aus
 * den Fahrzeugen und dem Fahrkurslayout. Das Systemmodell verwaltet
 * Komposita-Komponenten, die unbedingt vorhanden sein m�ssen.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface SystemModel
    extends ModelComponent {

  /**
   * Schl�ssel f�r die Fahrzeugflotte.
   */
  String VEHICLES = "Vehicles";
  /**
   * Sch�ssel f�r den Fahrkurs insgesamt.
   */
  String LAYOUT = "Layout";
  /**
   * Schl�ssel f�r die Fahrkurselemente.
   */
  String POINTS = "Points";
  String LOCATIONS = "Locations";
  String PATHS = "Paths";
  String LINKS = "Links";
  /**
   * Schl�ssel f�r die Stationstypen.
   */
  String LOCATION_TYPES = "LocationTypes";
  /**
   * Schl�ssel f�r die Blockbereiche.
   */
  String BLOCKS = "Blocks";
  /**
   * Key for groups.
   */
  String GROUPS = "Groups";
  /**
   * Schl�ssel f�r die statischen Routen.
   */
  String STATIC_ROUTES = "StaticRoutes";
  /**
   * Schl�ssel f�r andere grafische Objekte.
   */
  String OTHER_GRAPHICAL_ELEMENTS = "OtherGraphicalElements";

  /**
   * F�gt dem Systemmodell eine Hauptkomponente hinzu.
   *
   * @param key
   * @param component
   */
  void addMainFolder(String key, ModelComponent component);

  /**
   * Liefert die zum Schl�ssel passende Hauptkomponente.
   *
   * @param key
   * @return
   */
  ModelComponent getMainFolder(String key);

  /**
   * Liefert das Elternobjekt zu einem ModelComponent-Objekt. Die Zuordnung wird
   * �ber das Class-Objekt von item vorgenommen.
   *
   * @param item
   * @return
   */
  ModelComponent getFolder(ModelComponent item);

  /**
   * Liefert alle Objekt, die zu einer bestimmten Klasse geh�ren.
   *
   * @param foldername der Name des Ordners, in dem gesucht werden soll
   * @param classType die Klasse, von der die Objekte sein m�ssen
   * @return alle Objekte einer Klasse
   */
  <T> List<T> getAll(String foldername, Class<T> classType);

  /**
   * Liefert die Zuordnungstabelle.
   *
   * @return
   */
  EventDispatcher getEventDispatcher();

  /**
   * Setzt die Zuordnungstabelle zwischen Leitsteuerung und Modellierung /
   * Visualisierung.
   *
   * @param eventDispatcher
   */
  void setEventDispatcher(EventDispatcher eventDispatcher);

  /**
   * Erstellt eine neue leere Zeichnung.
   * <p>
   * Entwurfsmuster: Fabrikmethode
   *
   * @return die neu erzeugte Zeichnung
   */
  Drawing createDrawing();

  /**
   * Liefert die Zeichnung.
   *
   * @return die Zeichnung
   */
  Drawing getDrawing();

  /**
   * Liefert die Zeichenmethode. Hier sind beispielsweise m�glich "symbolisch"
   * und auf "Koordinaten basierend".
   *
   * @return
   */
  DrawingMethod getDrawingMethod();

  /**
   * Liefert eine Liste aller Fahrzeuge.
   *
   * @return
   */
  List<VehicleModel> getVehicleModels();

  /**
   * Sucht ein Fahrzeug mit einem bestimmten Namen.
   *
   * @param name der Name des gesuchten Fahrzeugs
   * @return das gefundene Fahrzeug oder
   * <code> null </code>, wenn kein entsprechendes Fahrzeug gefunden werden
   * konnte
   */
  VehicleModel getVehicleModel(String name);

  /**
   *
   * @return
   */
  List<LayoutModel> getLayoutModels();

  /**
   *
   * @param name
   * @return
   */
  LayoutModel getLayoutModel(String name);

  /**
   * Liefert eine Liste aller Knoten.
   *
   * @return
   */
  List<PointModel> getPointModels();

  /**
   * Sucht einen Knoten mit einem bestimmten Namen.
   *
   * @param name der Name des gesuchten Knotens
   * @return den gefundenen Knoten oder
   * <code> null </code>, wenn kein entsprechender Knoten gefunden werden konnte
   */
  PointModel getPointModel(String name);

  /**
   * Liefert alle Stationen (�bergabestationen, Batterieladestation,
   * Arbeitsstationen).
   *
   * @return eine Liste aller Stationen
   */
  List<LocationModel> getLocationModels();

  /**
   * Liefert alle Stationen, die zu einem bestimmten Typ geh�ren.
   *
   * @param locationType der Stationstyp
   * @return die Stationen
   */
  List<LocationModel> getLocationModels(LocationTypeModel locationType);

  /**
   * Sucht eine Station mit einem bestimmten Namen.
   *
   * @param name der Name der gesuchten Station
   * @return die gefundene Station oder
   * <code> null </code>, wenn keine entsprechende Station gefunden werden
   * konnte
   */
  LocationModel getLocationModel(String name);

  /**
   * Liefert alle Kanten zwischen zwei Punkten.
   *
   * @return eine Liste mit allen Kanten, die jeweils zwei Punkte miteinander
   * verbinden
   */
  List<PathModel> getPathModels();

  /**
   * Return the PathModel with the given name.
   *
   * @param name Name of the path.
   * @return The PathModel.
   */
  PathModel getPathModel(String name);

  /**
   * Liefert alle �Links, die jeweils einen Punkt mit einer Station verbinden.
   *
   * @return eine Liste aller Referenzen
   */
  List<LinkModel> getLinkModels();

  /**
   * Liefert alle Links auf Stationen eines bestimmten Typs.
   *
   * @param locationType der Stationstyp
   * @return eine Liste aller Links
   */
  List<LinkModel> getLinkModels(LocationTypeModel locationType);

  /**
   * Liefert alle Stationstypen.
   *
   * @return alle Stationstypen
   */
  List<LocationTypeModel> getLocationTypeModels();

  /**
   * Liefert den Stationstyp mit einem bestimmten Namen.
   *
   * @param name der Name des gesuchten Stationstyps
   * @return der gesuchte Stationstyp
   */
  LocationTypeModel getLocationTypeModel(String name);

  /**
   * Liefert alle Blockbereiche.
   *
   * @return eine Liste aller Blockbereiche
   */
  List<BlockModel> getBlockModels();

  /**
   * Liefert alle statischen Routen.
   *
   * @return eine Liste aller statischen Routen
   */
  List<StaticRouteModel> getStaticRouteModels();

  /**
   * Liefert alle grafischen Objekte, die lediglich eine illustrierende Wirkung
   * haben. Diese Objekte sind f�r den Fahrkurs irrelevant.
   *
   * @return eine Liste aller sonstigen grafischen Objekte, die mit dem Fahrkurs
   * direkt nichts zu tun haben
   */
  List<OtherGraphicalElement> getOtherGraphicalElements();
  
  /**
   * Adds a reference to a ModelLayoutElement to every object in the pool.
   *
   * @param layout
   * @param points
   * @param paths
   * @param locations
   * @param blocks
   */
  void createLayoutMap(VisualLayout layout,
                              Set<Point> points,
                              Set<Path> paths,
                              Set<Location> locations,
                              Set<Block> blocks);
  
  /**
   * Returns the LayoutMap.
   * 
   * @return The layout map.
   */
  Map<TCSObjectReference<?>, ModelLayoutElement> getLayoutMap();
}
