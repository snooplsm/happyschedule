def isPeak(id,stops,date):
	return None
    
def shouldIgnore(routeId, stops):
    startId = stops[0]["id"]
    endId = stops[len(stops)-1]["id"]
    if routeId=="15":
        if startId=="105" or endId=="105":
            return True
    if routeId=="8":
        if (startId=="105" and endId=="49") or (startId=="49" and endId=="105"):
            return True
        if startId=="105" or endId=="105":
            return True
        # if (startId=="49" and endId=="63") or (startId=="63" and endId=="49"):
        #     return True
    if routeId=="7" or routeId=="2" or routeId=="3":
        if "name" in stops[0]:
            print routeId, len(stops), stops[0]["name"],stops[0]["id"], "to", stops[len(stops)-1]["name"],stops[len(stops)-1]["id"]
        if startId=="145" and endId=="105":
            return False
        if endId=="145" and startId=="105":
            return False
        if startId=="35" and endId=="105":
            return False
        if endId=="35" and startId=="105":
            return False            
        if startId=="63" or endId=="63":
            return False        
        
    return False
            