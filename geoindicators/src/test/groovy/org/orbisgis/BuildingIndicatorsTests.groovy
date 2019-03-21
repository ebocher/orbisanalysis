package org.orbisgis

import org.orbisgis.datamanager.h2gis.H2GIS
import static org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BuildingIndicatorsTests {

    @Test
    void testGeometryProperties() {
        def h2GIS = H2GIS.open([databaseName: './target/buildingdb'])
        h2GIS.execute("""
                DROP TABLE IF EXISTS spatial_table, geom_properties;
                CREATE TABLE spatial_table (id int, the_geom LINESTRING);
                INSERT INTO spatial_table VALUES (1, 'LINESTRING(0 0, 0 10)'::GEOMETRY);
        """)
       def  p =  Geoclimate.BuildingIndicators.geometryProperties()
       p.execute([inputTableName: "spatial_table", inputFields:["id", "the_geom"], operations:["st_issimple","st_area", "area", "st_dimension"], outputTableName : "geom_properties",datasource:h2GIS])
        assert p.results.outputTableName == "geom_properties"
        h2GIS.getTable("geom_properties").eachRow {
            row -> assert row.the_geom!=null
                assert row.st_issimple==true
                assertEquals(0,row.st_area)
                assertEquals(1, row.st_dimension)
                assertEquals(1,  row.id)
        }
    }

    @Test
    void buildingSizeProperties() {
        def h2GIS = H2GIS.open([databaseName: './target/buildingdb'])
        String sqlString = new File(this.class.getResource("data_for_tests.sql").toURI()).text
        h2GIS.execute(sqlString)

        // Only the first 1 first created buildings are selected for the tests
        h2GIS.execute("DROP TABLE IF EXISTS tempo_build, building_size_properties; CREATE TABLE tempo_build AS SELECT * FROM building_test WHERE id_build = 7;")

        def  p =  Geoclimate.BuildingIndicators.buildingSizeProperties()
        p.execute([inputBuildingTableName: "tempo_build", inputFields:["id_build", "the_geom"],
                   operations:["building_volume", "building_floor_area", "building_total_facade_length",
                               "building_passive_volume_ratio"],
                   outputTableName : "building_size_properties",datasource:h2GIS])
        h2GIS.getTable("building_size_properties").eachRow {
            row ->
                assertEquals(141,row.building_volume)
                assertEquals(47, row.building_floor_area)
                assertEquals(38,  row.building_total_facade_length)
                assertEquals(0,  row.building_passive_volume_ratio)
        }
    }

    @Test
    void buildingNeighborsProperties() {
        def h2GIS = H2GIS.open([databaseName: './target/buildingdb'])
        String sqlString = new File(this.class.getResource("data_for_tests.sql").toURI()).text
        h2GIS.execute(sqlString)

        // Only the first 6 first created buildings are selected since any new created building may alter the results
        h2GIS.execute("DROP TABLE IF EXISTS tempo_build, building_neighbors_properties; CREATE TABLE tempo_build AS SELECT * " +
                "FROM building_test WHERE id_build < 7")

        def  p =  Geoclimate.BuildingIndicators.buildingNeighborsProperties()
        p.execute([inputBuildingTableName: "tempo_build", inputFields:["id_build", "the_geom"],
                   operations:["building_contiguity","building_common_wall_fraction",
                               "building_number_building_neighbor"],
                   outputTableName : "building_neighbors_properties",datasource:h2GIS])
        def concat = ["", "", ""]
        h2GIS.eachRow("SELECT * FROM building_neighbors_properties WHERE id_build = 1 OR id_build = 5 ORDER BY id_build ASC"){
            row ->
                concat[0]+= "${row.building_contiguity.round(5)}\n"
                concat[1]+= "${row.building_common_wall_fraction.round(5)}\n"
                concat[2]+= "${row.building_number_building_neighbor}\n"

        }
        assertEquals("0.0\n${(50/552).round(5)}\n".toString(),concat[0].toString())
        assertEquals("0.0\n${(10/46).round(5)}\n".toString(), concat[1].toString())
        assertEquals("0\n1\n".toString(),  concat[2].toString())
    }
}
