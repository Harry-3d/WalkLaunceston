# Recreational Trails

## fetching a list of all trails

/trails

Returns a list of all the trails in JSON format.
If no trails exist, returns an empty list.

Each trail has:
  * id
  * name
  * ???
  
What else do we need?  length? bounding area?

## Fetching a trail

/trail/{id}
e.g. /trail/123

Returns the .kml file for the trail with id
If no such trail exists, a 404 not found html status is returned.
