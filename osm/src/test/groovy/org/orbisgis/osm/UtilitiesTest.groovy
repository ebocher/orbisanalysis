/*
 * Bundle OSM is part of the OrbisGIS platform
 *
 * OrbisGIS is a java GIS application dedicated to research in GIScience.
 * OrbisGIS is developed by the GIS group of the DECIDE team of the
 * Lab-STICC CNRS laboratory, see <http://www.lab-sticc.fr/>.
 *
 * The GIS group of the DECIDE team is located at :
 *
 * Laboratoire Lab-STICC – CNRS UMR 6285
 * Equipe DECIDE
 * UNIVERSITÉ DE BRETAGNE-SUD
 * Institut Universitaire de Technologie de Vannes
 * 8, Rue Montaigne - BP 561 56017 Vannes Cedex
 *
 * OSM is distributed under LGPL 3 license.
 *
 * Copyright (C) 2019 CNRS (Lab-STICC UMR CNRS 6285)
 *
 *
 * OSM is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OSM is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * OSM. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.osm

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Envelope
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Polygon
import org.locationtech.jts.io.WKTReader
import org.orbisgis.osm.utils.OSMElement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.junit.jupiter.api.Assertions.*

/**
 * Test class for {@link Utilities}
 *
 * @author Sylvain PALOMINOS (UBS LAB-STICC 2019)
 */
class UtilitiesTest extends AbstractOSMTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UtilitiesTest)

    @BeforeEach
    final void beforeEach(TestInfo testInfo){
        LOGGER.info("@ ${testInfo.testMethod.get().name}()")
        super.beforeEach()
    }

    @AfterEach
    final void afterEach(TestInfo testInfo){
        super.afterEach()
        LOGGER.info("# ${testInfo.testMethod.get().name}()")
    }

    /**
     * Test the {@link Utilities#arrayToCoordinate(java.lang.Object)} method.
     */
    @Test
    void arrayToCoordinateTest(){
        def outer = []
        outer << [0.0, 0.0, 0.0]
        outer << [10.0, 0.0]
        outer << [10.0, 10.0, 0.0]
        outer << [0.0, 10.0]
        outer << [0.0, 0.0, 0.0]

        def coordinates = OSMTools.Utilities.arrayToCoordinate(outer)
        assertEquals 5, coordinates.size()
        assertEquals "(0.0, 0.0, 0.0)", coordinates[0].toString()
        assertEquals "(10.0, 0.0, NaN)", coordinates[1].toString()
        assertEquals "(10.0, 10.0, 0.0)", coordinates[2].toString()
        assertEquals "(0.0, 10.0, NaN)", coordinates[3].toString()
        assertEquals "(0.0, 0.0, 0.0)", coordinates[4].toString()
    }

    /**
     * Test the {@link Utilities#arrayToCoordinate(java.lang.Object)} method with bad data.
     */
    @Test
    void badArrayToCoordinateTest(){
        def array1 = OSMTools.Utilities.arrayToCoordinate(null)
        assertNotNull array1
        assertEquals 0, array1.length

        def array2 = OSMTools.Utilities.arrayToCoordinate([])
        assertNotNull array2
        assertEquals 0, array2.length

        def array3 = OSMTools.Utilities.arrayToCoordinate([[0]])
        assertNotNull array3
        assertEquals 0, array3.length

        def array4 = OSMTools.Utilities.arrayToCoordinate([[0, 1, 2, 3]])
        assertNotNull array4
        assertEquals 0, array4.length
    }

    /**
     * Test the {@link Utilities#parsePolygon(java.lang.Object, org.locationtech.jts.geom.GeometryFactory)}
     * method.
     */
    @Test
    void parsePolygonTest(){
        def outer = []
        outer << [0.0, 0.0, 0.0]
        outer << [10.0, 0.0]
        outer << [10.0, 10.0, 0.0]
        outer << [0.0, 10.0]
        outer << [0.0, 0.0, 0.0]

        def hole1 = []
        hole1 << [2.0, 2.0, 0.0]
        hole1 << [8.0, 2.0]
        hole1 << [8.0, 3.0]
        hole1 << [2.0, 2.0, 0.0]
        def hole2 = []
        hole2 << [2.0, 5.0, 0.0]
        hole2 << [8.0, 5.0]
        hole2 << [8.0, 7.0]
        hole2 << [2.0, 5.0, 0.0]

        def poly1 = []
        poly1 << outer

        def poly2 = []
        poly2 << outer
        poly2 << hole1
        poly2 << hole2

        assertEquals "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0))",
                OSMTools.Utilities.parsePolygon(poly1, new GeometryFactory()).toString()

        assertEquals "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (2 2, 8 2, 8 3, 2 2), (2 5, 8 5, 8 7, 2 5))",
                OSMTools.Utilities.parsePolygon(poly2, new GeometryFactory()).toString()
    }

    /**
     * Test the {@link Utilities#parsePolygon(java.lang.Object, org.locationtech.jts.geom.GeometryFactory)}
     * method with bad data.
     */
    @Test
    void badParsePolygonTest(){
        def outer = []
        outer << [0.0, 0.0, 0.0]
        outer << [10.0, 0.0]
        outer << [10.0, 10.0, 0.0]
        outer << [0.0, 10.0]
        def poly1 = []
        poly1 << outer

        assertNull OSMTools.Utilities.parsePolygon(null, new GeometryFactory())
        assertNull OSMTools.Utilities.parsePolygon([], new GeometryFactory())
        assertNull OSMTools.Utilities.parsePolygon([[]], new GeometryFactory())
        assertNull OSMTools.Utilities.parsePolygon([[null]], new GeometryFactory())
        assertNull OSMTools.Utilities.parsePolygon(poly1, new GeometryFactory())
    }

    /**
     * Test the {@link Utilities#executeNominatimQuery(java.lang.Object, java.lang.Object)} method.
     * This test performs a web request to the Nominatim service.
     */
    @Test
    void getExecuteNominatimQueryTest(){
        def path = RANDOM_PATH()
        def file = new File(path)
        assertTrue OSMTools.Utilities.executeNominatimQuery("vannes", file)
        assertTrue file.exists()
        assertFalse file.text.isEmpty()
    }

    /**
     * Test the {@link Utilities#executeNominatimQuery(java.lang.Object, java.lang.Object)} method.
     */
    @Test
    void badGetExecuteNominatimQueryTest(){
        def file = new File(RANDOM_PATH())
        assertFalse OSMTools.Utilities.executeNominatimQuery(null, file)
        assertFalse OSMTools.Utilities.executeNominatimQuery("", file)
        assertFalse OSMTools.Utilities.executeNominatimQuery("query", file.getAbsolutePath())
        badExecuteNominatimQueryOverride()
        assertFalse OSMTools.Utilities.executeNominatimQuery("query", file)
    }

    /**
     * Test the {@link Utilities#toBBox(org.locationtech.jts.geom.Geometry)} method.
     */
    @Test
    void toBBoxTest(){
        def factory = new GeometryFactory()
        def point = factory.createPoint(new Coordinate(1.3, 7.7))
        Coordinate[] coordinates = [new Coordinate(2.0, 2.0),
                                    new Coordinate(4.0, 2.0),
                                    new Coordinate(4.0, 4.0),
                                    new Coordinate(2.0, 4.0),
                                    new Coordinate(2.0, 2.0)]
        def ring = factory.createLinearRing(coordinates)
        def polygon = factory.createPolygon(ring)

        assertEquals "(bbox:7.7,1.3,7.7,1.3)", OSMTools.Utilities.toBBox(point)
        assertEquals "(bbox:2.0,2.0,4.0,4.0)", OSMTools.Utilities.toBBox(ring)
        assertEquals "(bbox:2.0,2.0,4.0,4.0)", OSMTools.Utilities.toBBox(polygon)
    }

    /**
     * Test the {@link Utilities#toBBox(org.locationtech.jts.geom.Geometry)} method with bad data.
     */
    @Test
    void badToBBoxTest(){
        assertNull OSMTools.Utilities.toBBox(null)
    }

    /**
     * Test the {@link Utilities#toPoly(org.locationtech.jts.geom.Geometry)} method.
     */
    @Test
    void toPolyTest(){
        def factory = new GeometryFactory()
        Coordinate[] coordinates = [new Coordinate(2.0, 2.0),
                                    new Coordinate(4.0, 2.0),
                                    new Coordinate(4.0, 4.0),
                                    new Coordinate(2.0, 4.0),
                                    new Coordinate(2.0, 2.0)]
        def ring = factory.createLinearRing(coordinates)
        def poly = factory.createPolygon(ring)
        assertGStringEquals "(poly:\"2.0 2.0 2.0 4.0 4.0 4.0 4.0 2.0\")", OSMTools.Utilities.toPoly(poly)
        }

    /**
     * Test the {@link Utilities#toPoly(org.locationtech.jts.geom.Geometry)} method with bad data.
     */
    @Test
    void badToPolyTest(){
        def factory = new GeometryFactory()
        assertNull OSMTools.Utilities.toPoly(null)
        assertNull OSMTools.Utilities.toPoly(factory.createPoint(new Coordinate(0.0, 0.0)))
        assertNull OSMTools.Utilities.toPoly(factory.createPolygon())
    }

    /**
     * Test the {@link Utilities#buildOSMQuery(org.locationtech.jts.geom.Envelope, java.lang.Object, org.orbisgis.osm.utils.OSMElement[])}
     * method.
     */
    @Test
    void buildOSMQueryFromEnvelopeTest(){
        def enveloppe = new Envelope(0.0, 2.3, 7.6, 8.9)
        assertEquals "[bbox:7.6,0.0,8.9,2.3];\n" +
                "(\n" +
                "\tnode[\"building\"];\n" +
                "\tnode[\"water\"];\n" +
                "\tway[\"building\"];\n" +
                "\tway[\"water\"];\n" +
                ");\n" +
                "(._;>;);\n" +
                "out;", OSMTools.Utilities.buildOSMQuery(enveloppe, ["building", "water"], OSMElement.NODE, OSMElement.WAY)
        assertEquals "[bbox:7.6,0.0,8.9,2.3];\n" +
                "(\n" +
                ");\n" +
                "(._;>;);\n" +
                "out;", OSMTools.Utilities.buildOSMQuery(enveloppe, ["building", "water"])
        assertEquals "[bbox:7.6,0.0,8.9,2.3];\n" +
                "(\n" +
                ");\n" +
                "(._;>;);\n" +
                "out;", OSMTools.Utilities.buildOSMQuery(enveloppe, ["building", "water"], null)
        assertEquals "[bbox:7.6,0.0,8.9,2.3];\n" +
                "(\n" +
                ");\n" +
                "(._;>;);\n" +
                "out;", OSMTools.Utilities.buildOSMQuery(enveloppe, ["building", "water"], null)
        assertEquals "[bbox:7.6,0.0,8.9,2.3];\n" +
                "(\n" +
                "\tnode;\n" +
                "\tway;\n" +
                ");\n" +
                "(._;>;);\n" +
                "out;", OSMTools.Utilities.buildOSMQuery(enveloppe, [], OSMElement.NODE, OSMElement.WAY)
    }

    /**
     * Test the {@link Utilities#buildOSMQuery(org.locationtech.jts.geom.Envelope, java.lang.Object, org.orbisgis.osm.utils.OSMElement[])}
     * method with bad data.
     */
    @Test
    void badBuildOSMQueryFromEnvelopeTest(){
        assertNull OSMTools.Utilities.buildOSMQuery((Envelope)null, ["building"], OSMElement.NODE)
    }

    /**
     * Test the {@link Utilities#buildOSMQuery(org.locationtech.jts.geom.Polygon, java.lang.Object, org.orbisgis.osm.utils.OSMElement[])}
     * method.
     */
    @Test
    void buildOSMQueryFromPolygonTest(){
        def factory = new GeometryFactory();
        Coordinate[] coordinates = [
                new Coordinate(0.0, 2.3),
                new Coordinate(7.6, 2.3),
                new Coordinate(7.6, 8.9),
                new Coordinate(0.0, 8.9),
                new Coordinate(0.0, 2.3)
        ]
        def ring = factory.createLinearRing(coordinates)
        def polygon = factory.createPolygon(ring)
        assertEquals "[bbox:2.3,0.0,8.9,7.6];\n" +
                "(\n" +
                "\tnode[\"building\"](poly:\"2.3 0.0 2.3 7.6 8.9 7.6 8.9 0.0\");\n" +
                "\tnode[\"water\"](poly:\"2.3 0.0 2.3 7.6 8.9 7.6 8.9 0.0\");\n" +
                "\tway[\"building\"](poly:\"2.3 0.0 2.3 7.6 8.9 7.6 8.9 0.0\");\n" +
                "\tway[\"water\"](poly:\"2.3 0.0 2.3 7.6 8.9 7.6 8.9 0.0\");\n" +
                ");\n" +
                "(._;>;);\n" +
                "out;", OSMTools.Utilities.buildOSMQuery(polygon, ["building", "water"], OSMElement.NODE, OSMElement.WAY)
        assertEquals "[bbox:2.3,0.0,8.9,7.6];\n" +
                "(\n" +
                ");\n" +
                "(._;>;);\n" +
                "out;", OSMTools.Utilities.buildOSMQuery(polygon, ["building", "water"])
        assertEquals "[bbox:2.3,0.0,8.9,7.6];\n" +
                "(\n" +
                ");\n" +
                "(._;>;);\n" +
                "out;", OSMTools.Utilities.buildOSMQuery(polygon, ["building", "water"], null)
        assertEquals "[bbox:2.3,0.0,8.9,7.6];\n" +
                "(\n" +
                ");\n" +
                "(._;>;);\n" +
                "out;", OSMTools.Utilities.buildOSMQuery(polygon, ["building", "water"], null)
        assertEquals "[bbox:2.3,0.0,8.9,7.6];\n" +
                "(\n" +
                "\tnode(poly:\"2.3 0.0 2.3 7.6 8.9 7.6 8.9 0.0\");\n" +
                "\tway(poly:\"2.3 0.0 2.3 7.6 8.9 7.6 8.9 0.0\");\n" +
                ");\n" +
                "out;", OSMTools.Utilities.buildOSMQuery(polygon, [], OSMElement.NODE, OSMElement.WAY)
    }

    /**
     * Test the {@link Utilities#buildOSMQuery(org.locationtech.jts.geom.Polygon, java.lang.Object, org.orbisgis.osm.utils.OSMElement[])}
     * method with bad data.
     */
    @Test
    void badBuildOSMQueryFromPolygonTest(){
        assertNull OSMTools.Utilities.buildOSMQuery((Polygon)null, ["building"], OSMElement.NODE)
        assertNull OSMTools.Utilities.buildOSMQuery(new GeometryFactory().createPolygon(), ["building"], OSMElement.NODE)
    }

    /**
     * Test the {@link Utilities#readJSONParameters(java.lang.String)} method.
     */
    @Test
    void readJSONParametersTest(){
        def map = [
                "tags" : [
                        "highway", "cycleway", "biclycle_road", "cyclestreet", "route", "junction"
                ],
                "columns":["width","highway", "surface", "sidewalk",
                        "lane","layer","maxspeed","oneway",
                        "h_ref","route","cycleway",
                        "biclycle_road","cyclestreet","junction"
                ]
        ]
        assertEquals map, OSMTools.Utilities.readJSONParameters(new File(UtilitiesTest.getResource("road_tags.json").toURI()).absolutePath)
        assertEquals map, OSMTools.Utilities.readJSONParameters(UtilitiesTest.getResourceAsStream("road_tags.json"))
    }

    /**
     * Test the {@link Utilities#readJSONParameters(java.lang.Object)} method with bad data.
     */
    @Test
    void badReadJSONParametersTest(){
        assertNull OSMTools.Utilities.readJSONParameters(null)
        assertNull OSMTools.Utilities.readJSONParameters("")
        assertNull OSMTools.Utilities.readJSONParameters("toto")
        assertNull OSMTools.Utilities.readJSONParameters("target")
        assertNull OSMTools.Utilities.readJSONParameters(new File(UtilitiesTest.getResource("bad_json_params.json").toURI()).absolutePath)
        assertNull OSMTools.Utilities.readJSONParameters(UtilitiesTest.getResourceAsStream("bad_json_params.json"))
    }

    /**
     * Test the {@link Utilities#buildGeometryAndZone(org.locationtech.jts.geom.Geometry, int, java.lang.Object)} and
     * {@link Utilities#buildGeometryAndZone(org.locationtech.jts.geom.Geometry, int, int, java.lang.Object)} methods.
     */
    @Test
    void buildGeometryAndZoneTest(){
        def ds = RANDOM_DS()
        WKTReader wktReader =  new WKTReader()
        GeometryFactory gf = new GeometryFactory()
        // Input geometry in WGS84
        def polygonIn4326 = wktReader.read("POLYGON ((-3.0162996512745006 48.820485589787296, -3.0164426870106715 48.82105773273198, -3.016090206089393 48.82110881692347, -3.0155589304979 48.82111392534262, -3.015323943217047 48.820562216074535, -3.0157428335872627 48.82033744563198, -3.0157428335872627 48.82033744563198, -3.016059555574499 48.820168867800064, -3.016059555574499 48.820168867800064, -3.0162996512745006 48.820485589787296))")
        def result = OSMTools.Utilities.buildGeometryAndZone(polygonIn4326, 0, ds)
        assertEquals 3, result.size()
        assertTrue result.containsKey("geomInMetric")
        assertTrue result.containsKey("filterAreaInMetric")
        assertTrue result.containsKey("filterAreaInLatLong")
        assertTrue wktReader.read("POLYGON ((498803.5237285082 5407500.456403683, 498793.03792451497 5407564.05909038, 498818.9127002239 5407569.732171688, 498857.9106295072 5407570.291925942, 498875.14731501095 5407508.959441491, 498844.3935377365 5407483.979834437, 498844.3935377365 5407483.979834437, 498821.1405313526 5407465.245280385, 498821.1405313526 5407465.245280385, 498803.5237285082 5407500.456403683))").equals(result.geomInMetric)
        assertTrue result.geomInMetric.getEnvelopeInternal().equals(result.filterAreaInMetric.getEnvelopeInternal())
        assertTrue polygonIn4326.getEnvelopeInternal().equals(result.filterAreaInLatLong.getEnvelopeInternal())

        // Bad input geometry
        def polygonNotIn4326 = wktReader.read("POLYGON ((498800.6666666667 5407541.666666667, 498815 5407541.666666667, 498815 5407514.666666667, 498800.6666666667 5407514.666666667, 498800.6666666667 5407541.666666667))")
        result = OSMTools.Utilities.buildGeometryAndZone(polygonNotIn4326, 0, ds)
        assertNull(result)

        // Input geometry in WGS84 plus distance
        result = OSMTools.Utilities.buildGeometryAndZone(polygonIn4326, 100, ds)
        assertEquals 3, result.size()
        assertTrue result.containsKey("geomInMetric")
        assertTrue result.containsKey("filterAreaInMetric")
        assertTrue result.containsKey("filterAreaInLatLong")
        assertTrue wktReader.read("POLYGON ((498803.5237285082 5407500.456403683, 498793.03792451497 5407564.05909038, 498818.9127002239 5407569.732171688, 498857.9106295072 5407570.291925942, 498875.14731501095 5407508.959441491, 498844.3935377365 5407483.979834437, 498844.3935377365 5407483.979834437, 498821.1405313526 5407465.245280385, 498821.1405313526 5407465.245280385, 498803.5237285082 5407500.456403683))").equals(result.geomInMetric)
        def geomEnv = result.geomInMetric.getEnvelopeInternal()
        geomEnv.expandBy(100)
        assertTrue geomEnv.equals(result.filterAreaInMetric.getEnvelopeInternal())
        assertTrue wktReader.read("POLYGON ((-3.0178043735867743 48.81926902159189, -3.0178053455549443 48.82201318989658, -3.0139620396939995 48.822013719431, -3.013961277526342 48.81926955107547, -3.0178043735867743 48.81926902159189))").equals(result.filterAreaInLatLong)


        // Input geometry in EPSG 2154 plus distance
        result = OSMTools.Utilities.buildGeometryAndZone(polygonIn4326, 2154, 100, ds)
        assertEquals 3, result.size()
        assertTrue result.containsKey("geomInMetric")
        assertTrue result.containsKey("filterAreaInMetric")
        assertTrue result.containsKey("filterAreaInLatLong")
        assertTrue wktReader.read("POLYGON ((258682.89453413076 6874645.141617264, 258677.2652194511 6874709.373873287, 258703.50296784262 6874713.067557089, 258742.44127644302 6874710.664920253, 258754.97613136837 6874648.184352494, 258722.40631679696 6874625.605158827, 258722.40631679696 6874625.605158827, 258697.79179186112 6874608.685161716, 258697.79179186112 6874608.685161716, 258682.89453413076 6874645.141617264))").equals(result.geomInMetric)
        geomEnv = result.geomInMetric.getEnvelopeInternal()
        geomEnv.expandBy(100)
        assertTrue geomEnv.equals(result.filterAreaInMetric.getEnvelopeInternal())
        assertTrue wktReader.read("POLYGON ((-3.01759259242928 48.81918961082766, -3.017908217509856 48.821919127879745, -3.014136819140715 48.82210923114748, -3.0138213903284097 48.81937970460556, -3.01759259242928 48.81918961082766))").equals(result.filterAreaInLatLong)

    }

    /**
     * Test the {@link Utilities#buildGeometryAndZone(org.locationtech.jts.geom.Geometry, int, java.lang.Object)} and
     * {@link Utilities#buildGeometryAndZone(org.locationtech.jts.geom.Geometry, int, int, java.lang.Object)} methods
     * with bad data.
     */
    @Test
    void badBuildGeometryAndZoneTest(){
        def ds = RANDOM_DS()
        def factory = new GeometryFactory()
        Coordinate[] coordinates = [
                new Coordinate(0, 0),
                new Coordinate(5, 0),
                new Coordinate(5, 8),
                new Coordinate(0, 8),
                new Coordinate(0, 0)
        ]
        def ring = factory.createLinearRing(coordinates)
        def polygon0 = factory.createPolygon(ring)
        assertNull OSMTools.Utilities.buildGeometryAndZone(null, 0, ds)
        assertNull OSMTools.Utilities.buildGeometryAndZone(polygon0, 0, null)
        assertNull OSMTools.Utilities.buildGeometryAndZone(null, 0, 0, ds)
        assertNull OSMTools.Utilities.buildGeometryAndZone(polygon0, 0, 0, null)
    }

    /**
     * Test the {@link Utilities#buildGeometry(java.lang.Object)} method.
     */
    @Test
    void buildGeometryTest(){
        assertEquals("POLYGON ((-3.29109 48.72223, -3.29109 48.83535, -2.80357 48.83535, -2.80357 48.72223, -3.29109 48.72223))",
                OSMTools.Utilities.buildGeometry([-3.29109,48.83535,-2.80357,48.72223]).toString())
    }

    /**
     * Test the {@link Utilities#buildGeometry(java.lang.Object)} method with bad data.
     */
    @Test
    void badBuildGeometryTest(){
        assertNull OSMTools.Utilities.buildGeometry([-3.29109,48.83535,-2.80357])
        assertNull OSMTools.Utilities.buildGeometry([-Float.MAX_VALUE,48.83535,-2.80357,48.72223])
        assertNull OSMTools.Utilities.buildGeometry([-3.29109,Float.MAX_VALUE,-2.80357,48.72223])
        assertNull OSMTools.Utilities.buildGeometry([-3.29109,48.83535,-Float.MAX_VALUE,48.72223])
        assertNull OSMTools.Utilities.buildGeometry([-3.29109,48.83535,-2.80357,Float.MAX_VALUE])
        assertNull OSMTools.Utilities.buildGeometry(null)
        assertNull OSMTools.Utilities.buildGeometry()
        assertNull OSMTools.Utilities.buildGeometry(new GeometryFactory())
    }

    /**
     * Test the {@link Utilities#geometryFromNominatim(java.lang.Object)} method.
     */
    @Test
    void geometryFromNominatimTest(){
        assertEquals("POLYGON ((-3.29109 48.72223, -3.29109 48.83535, -2.80357 48.83535, -2.80357 48.72223, -3.29109 48.72223))",
                OSMTools.Utilities.geometryFromNominatim([48.83535,-3.29109,48.72223,-2.80357]).toString())
    }

    /**
     * Test the {@link Utilities#geometryFromNominatim(java.lang.Object)} method with bad data.
     */
    @Test
    void badGeometryFromNominatimTest(){
        assertNull OSMTools.Utilities.geometryFromNominatim([-3.29109,48.83535,-2.80357])
        assertNull OSMTools.Utilities.geometryFromNominatim(null)
        assertNull OSMTools.Utilities.geometryFromNominatim()
        assertNull OSMTools.Utilities.geometryFromNominatim(new GeometryFactory())
    }

    /**
     * Test the {@link Utilities#geometryFromOverpass(java.lang.Object)} method.
     */
    @Test
    void geometryFromOverpassTest(){
        assertEquals("POLYGON ((-3.29109 48.72223, -3.29109 48.83535, -2.80357 48.83535, -2.80357 48.72223, -3.29109 48.72223))",
                OSMTools.Utilities.geometryFromOverpass([48.83535,-3.29109,48.72223,-2.80357]).toString())
    }

    /**
     * Test the {@link Utilities#geometryFromOverpass(java.lang.Object)} method with bad data.
     */
    @Test
    void badGeometryFromOverpassTest(){
        assertNull OSMTools.Utilities.geometryFromOverpass([-3.29109,48.83535,-2.80357])
        assertNull OSMTools.Utilities.geometryFromOverpass(null)
        assertNull OSMTools.Utilities.geometryFromOverpass()
        assertNull OSMTools.Utilities.geometryFromOverpass(new GeometryFactory())
    }

    /**
     * Test the {@link Utilities#dropOSMTables(java.lang.String, org.orbisgis.orbisdata.datamanager.jdbc.JdbcDataSource)}
     * method.
     */
    @Test
    void dropOSMTablesTest(){
        def ds = RANDOM_DS()
        ds.execute "CREATE TABLE prefix_node"
        ds.execute "CREATE TABLE prefix_node_member"
        ds.execute "CREATE TABLE prefix_node_tag"
        ds.execute "CREATE TABLE prefix_relation"
        ds.execute "CREATE TABLE prefix_relation_member"
        ds.execute "CREATE TABLE prefix_relation_tag"
        ds.execute "CREATE TABLE prefix_way"
        ds.execute "CREATE TABLE prefix_way_member"
        ds.execute "CREATE TABLE prefix_way_node"
        ds.execute "CREATE TABLE prefix_way_tag"
        assertTrue OSMTools.Utilities.dropOSMTables("prefix", ds)

        ds.execute "CREATE TABLE _node"
        ds.execute "CREATE TABLE _node_member"
        ds.execute "CREATE TABLE _node_tag"
        ds.execute "CREATE TABLE _relation"
        ds.execute "CREATE TABLE _relation_member"
        ds.execute "CREATE TABLE _relation_tag"
        ds.execute "CREATE TABLE _way"
        ds.execute "CREATE TABLE _way_member"
        ds.execute "CREATE TABLE _way_node"
        ds.execute "CREATE TABLE _way_tag"
        assertTrue OSMTools.Utilities.dropOSMTables("", ds)

    }

    /**
     * Test the {@link Utilities#dropOSMTables(java.lang.String, org.orbisgis.orbisdata.datamanager.jdbc.JdbcDataSource)}
     * method with bad data.
     */
    @Test
    void badDropOSMTablesTest(){
        def ds = RANDOM_DS()
        assertFalse OSMTools.Utilities.dropOSMTables("prefix", null)
        assertFalse OSMTools.Utilities.dropOSMTables(null, ds)
    }

    /**
     * Test the {@link Utilities#getAreaFromPlace(java.lang.Object)} method.
     */
    @Test
    void getAreaFromPlaceTest(){
        sampleExecuteNominatimPolygonQueryOverride()
        assertEquals "POLYGON ((0 0, 0 2, 2 2, 2 2, 2 0, 0 0))", OSMTools.Utilities.getAreaFromPlace("Place name").toString()
        assertEquals "Place name", query
        sampleExecuteNominatimMultipolygonQueryOverride()
        assertEquals "MULTIPOLYGON (((0 0, 0 2, 2 2, 2 2, 2 0, 0 0)), ((3 3, 3 4, 4 4, 4 3, 3 3)))", OSMTools.Utilities.getAreaFromPlace("Place name").toString()
        assertEquals "Place name", query
    }

    /**
     * Test the {@link Utilities#getAreaFromPlace(java.lang.Object)} method with bad data.
     */
    @Test
    void badGetAreaFromPlaceTest(){
        sampleExecuteNominatimPolygonQueryOverride()
        assertNull OSMTools.Utilities.getAreaFromPlace(null)
        badExecuteNominatimQueryOverride()
        assertNull OSMTools.Utilities.getAreaFromPlace("place")
    }

    @Test
    void getPlaceAndEPSG(){
        def  placeName = "Boston"
        def targetEPSG=-1
        def ds = RANDOM_DS()
        Geometry geom = OSMTools.Utilities.getAreaFromPlace(placeName);
        assertNotNull(geom)
        def geomAndEnv = OSMTools.Utilities.buildGeometryAndZone(geom, targetEPSG, 0, ds)
        assertEquals(32619, geomAndEnv.geomInMetric.getSRID())
        placeName = "Paimpol"
        geom = OSMTools.Utilities.getAreaFromPlace(placeName);
        assertNotNull(geom)
        geomAndEnv = OSMTools.Utilities.buildGeometryAndZone(geom, targetEPSG, 0, ds)
        assertEquals(32630, geomAndEnv.geomInMetric.getSRID())
        placeName = "Paimpol"
        geom = OSMTools.Utilities.getAreaFromPlace(placeName);
        assertNotNull(geom)
        geomAndEnv = OSMTools.Utilities.buildGeometryAndZone(geom, targetEPSG, 0, ds)
        assertEquals(32630, geomAndEnv.geomInMetric.getSRID())

    }
}
