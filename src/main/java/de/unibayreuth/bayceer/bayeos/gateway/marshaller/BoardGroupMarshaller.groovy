package de.unibayreuth.bayceer.bayeos.gateway.marshaller

import de.unibayreuth.bayceer.bayeos.gateway.model.BoardGroup
import groovy.json.JsonBuilder

class BoardGroupMarshaller {
    
    public static String toGeoJSON(BoardGroup bg) {                        
        def builder = new JsonBuilder()        
        builder {
            type "FeatureCollection"
            features bg.boards.collect { s ->
                [
                    type: 'Feature',
                    properties: [
                        id: s.id,
                        name: s.name,
                        origin: s.origin,
                        dbFolderID: s.dbFolderId
                    ],
                    geometry: [
                        type: 'Point',
                        coordinates: [s.getLon(),s.getLat()]
                    ]
                ]
            }
        }                
        return builder.toPrettyString()                           
    }
}


    