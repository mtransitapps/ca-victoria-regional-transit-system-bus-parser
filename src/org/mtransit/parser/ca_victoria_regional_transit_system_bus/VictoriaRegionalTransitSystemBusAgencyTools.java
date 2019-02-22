package org.mtransit.parser.ca_victoria_regional_transit_system_bus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.commons.StrategicMappingCommons;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

// https://www.bctransit.com/open-data
// https://victoria.mapstrat.com/current/google_transit.zip
public class VictoriaRegionalTransitSystemBusAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-victoria-regional-transit-system-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new VictoriaRegionalTransitSystemBusAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("\nGenerating Victoria Regional TS bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this, true);
		super.start(args);
		System.out.printf("\nGenerating Victoria Regional TS bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIds != null && this.serviceIds.isEmpty();
	}

	private static final String INCLUDE_ONLY_SERVICE_ID_STARTS_WITH = null;
	private static final String INCLUDE_ONLY_SERVICE_ID_STARTS_WITH2 = null;

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (INCLUDE_ONLY_SERVICE_ID_STARTS_WITH != null && !gCalendar.getServiceId().startsWith(INCLUDE_ONLY_SERVICE_ID_STARTS_WITH)
				&& INCLUDE_ONLY_SERVICE_ID_STARTS_WITH2 != null && !gCalendar.getServiceId().startsWith(INCLUDE_ONLY_SERVICE_ID_STARTS_WITH2)) {
			return true;
		}
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (INCLUDE_ONLY_SERVICE_ID_STARTS_WITH != null && !gCalendarDates.getServiceId().startsWith(INCLUDE_ONLY_SERVICE_ID_STARTS_WITH)
				&& INCLUDE_ONLY_SERVICE_ID_STARTS_WITH2 != null && !gCalendarDates.getServiceId().startsWith(INCLUDE_ONLY_SERVICE_ID_STARTS_WITH2)) {
			return true;
		}
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	private static final String INCLUDE_AGENCY_ID = "1"; // Victoria Regional Transit System only

	@Override
	public boolean excludeRoute(GRoute gRoute) {
		if (!INCLUDE_AGENCY_ID.equals(gRoute.getAgencyId())) {
			return true;
		}
		return super.excludeRoute(gRoute);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (INCLUDE_ONLY_SERVICE_ID_STARTS_WITH != null && !gTrip.getServiceId().startsWith(INCLUDE_ONLY_SERVICE_ID_STARTS_WITH)
				&& INCLUDE_ONLY_SERVICE_ID_STARTS_WITH2 != null && !gTrip.getServiceId().startsWith(INCLUDE_ONLY_SERVICE_ID_STARTS_WITH2)) {
			return true;
		}
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public long getRouteId(GRoute gRoute) {
		return Long.parseLong(gRoute.getRouteShortName()); // use route short name as route ID
	}

	@Override
	public String getRouteLongName(GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongName();
		routeLongName = CleanUtils.cleanNumbers(routeLongName);
		routeLongName = CleanUtils.cleanStreetTypes(routeLongName);
		return CleanUtils.cleanLabel(routeLongName);
	}

	private static final String AGENCY_COLOR_GREEN = "34B233";// GREEN (from PDF Corporate Graphic Standards)
	private static final String AGENCY_COLOR_BLUE = "002C77"; // BLUE (from PDF Corporate Graphic Standards)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Override
	public String getRouteColor(GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.getRouteColor())) {
			return AGENCY_COLOR_BLUE;
		}
		return super.getRouteColor(gRoute);
	}

	private static final String AND = " & ";
	private static final String EXCH = "Exch";

	private static final String DOWNTOWN = "Downtown";
	private static final String OAK_BAY = "Oak Bay";
	private static final String SOUTH_OAK_BAY = "South " + OAK_BAY;
	private static final String ROYAL_OAK = "Royal Oak";
	private static final String ROYAL_OAK_EXCH = ROYAL_OAK + " " + EXCH;
	private static final String CAMOSUN = "Camosun";
	private static final String JAMES_BAY = "James Bay";
	private static final String DOCKYARD = "Dockyard";
	private static final String ADMIRALS_WALK = "Admirals Walk";
	private static final String HILLSIDE = "Hillside";
	private static final String HILLSIDE_MALL = HILLSIDE + " Mall";
	private static final String U_VIC = "UVic";
	private static final String BRENTWOOD = "Brentwood";
	private static final String SAANICHTON = "Saanichton";
	private static final String SAANICHTON_EXCH = SAANICHTON + " " + EXCH;
	private static final String SWARTZ_BAY = "Swartz Bay";
	private static final String SWARTZ_BAY_FERRY = SWARTZ_BAY + " Ferry";
	private static final String SOOKE = "Sooke";
	private static final String LANGFORD = "Langford";
	private static final String LANGFORD_EXCH = LANGFORD + " " + EXCH;
	private static final String COLWOOD_EXCH = "Colwood " + EXCH;
	private static final String HAPPY_VLY = "Happy Vly";
	private static final String TILLICUM_MALL = "Tillicum Mall";
	private static final String SPECTRUM_SCHOOL = "Spectrum School";
	private static final String GORGE = "Gorge";
	private static final String INTERURBAN = "Interurban";
	private static final String MILE_HOUSE = "Mile House";
	private static final String VERDIER = "Verdier";
	private static final String SIDNEY = "Sidney";
	private static final String BEAR_MOUTAIN = "Bear Mtn";
	private static final String MC_DONALD_PARK = "McDonald Pk";
	private static final String MC_TAVISH = "McTavish";
	private static final String MC_TAVISH_EXCH = MC_TAVISH + " " + EXCH;
	private static final String VIC_GENERAL = "Vic General";
	private static final String UPTOWN = "Uptown";
	private static final String RICHMOND = "Richmond";
	private static final String MC_KENZIE = "McKenzie";
	private static final String DOUGLAS = "Douglas";
	private static final String WILLOWS = "Willows";
	private static final String WESTHILLS_EXCH = "Westhills Exch";
	private static final String KEATING = "Keating";
	private static final String SHORELINE_SCHOOL = "Shoreline Sch";
	private static final String R_JUBILEE = "R. Jubilee";
	private static final String VIC_WEST = "Vic West";

	private static HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;
	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<Long, RouteTripSpec>();
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId, List<MTripStop> list1, List<MTripStop> list2, MTripStop ts1, MTripStop ts2, GStop ts1GStop, GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@Override
	public ArrayList<MTrip> splitTrip(MRoute mRoute, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@Override
	public Pair<Long[], Integer[]> splitTripStop(MRoute mRoute, GTrip gTrip, GTripStop gTripStop, ArrayList<MTrip> splitTrips, GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		if (mRoute.getId() == 1L) {
			if (gTrip.getDirectionId() == 0) { // DOWNTOWN - WEST
				if (Arrays.asList( //
						"Downtown" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // SOUTH OAK BAY - EAST
				if (Arrays.asList( //
						"South Oak Bay via Richardson" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mRoute.getId() == 2L) {
			if (gTrip.getDirectionId() == 0) { // JAMES BAY - WEST
				if (Arrays.asList( //
						"James Bay - Fisherman's Wharf" // <>
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // OAK BAY - EAST
				if (Arrays.asList( //
						"James Bay - Fisherman's Wharf", // <>
						"Downtown", //
						"South Oak Bay - Oak Bay Village", //
						"Willows - Oak Bay Village" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mRoute.getId() == 3L) {
			if (gTrip.getDirectionId() == 0) { // JAMES BAY - CLOCKWISE
				if (Arrays.asList( //
						"Downtown Only", //
						"James Bay To 10 R. Jubilee", //
						"James Bay - Linden to 10 R. Jubilee", //
						"James Bay - Quimper To 10 R. Jubilee" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // ROYAL JUBILEE - COUNTERCLOCKWISE
				if (Arrays.asList( //
						"Royal Jubilee - Cook St Village", //
						"Royal Jubilee - Cook St Vlg/Quimper" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.COUNTERCLOCKWISE);
					return;
				}
			}
		} else if (mRoute.getId() == 4L) {
			if (gTrip.getDirectionId() == 0) { // DOWNTOWN - WEST
				if (Arrays.asList( //
						"Downtown", //
						"To Gorge & Douglas" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - EAST
				if (Arrays.asList( //
						"UVic Via Hillside" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mRoute.getId() == 6L) {
			if (gTrip.getDirectionId() == 0) { // ROYAL OAK - NORTH
				if (Arrays.asList( //
						"Royal Oak Exch Via Royal Oak Mall", //
						"6A Royal Oak Exch Via Emily Carr", //
						"6B Royal Oak Exch Via Chatterton" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - SOUTH
				if (Arrays.asList( //
						"Downtown", //
						"6B Downtown Via Chatterton", //
						"6A Downtown Via Emily Carr" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (mRoute.getId() == 7L) {
			if (gTrip.getDirectionId() == 0) { // DOWNTOWN - CLOCKWISE
				if (Arrays.asList( //
						"Downtown Only", //
						"7N Downtown Only", //
						"Downtown - To 21 Interurban", //
						"7N Downtown - To 21 Interurban", //
						"Downtown To 21 Interurban" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - COUNTERCLOCKWISE
				if (Arrays.asList( //
						"UVic Via Fairfield", //
						"7N UVic - Cook St Village" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.COUNTERCLOCKWISE);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 8L) {
			if (gTrip.getDirectionId() == 0) { // INTERURBAN - WEST
				if (Arrays.asList( //
						"Tillicum Mall Via Finalyson", //
						"Interurban Via Finlayson" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // OAK BAY - EAST
				if (Arrays.asList( //
						"To Richmond & Oak Bay Ave Only", //
						"To Douglas Only - Mayfair Mall", //
						"Oak Bay Via Finalyson" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 9L) {
			if (gTrip.getDirectionId() == 0) { // ROYAL OAK - WEST
				if (Arrays.asList( //
						"Royal Oak Exch - Hillside/Gorge" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - EAST
				if (Arrays.asList( //
						"UVic - Gorge/Hillside" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 10L) {
			if (gTrip.getDirectionId() == 0) { // ROYAL JUBILEE - CLOCKWISE
				if (Arrays.asList( //
						"Royal Jubilee Via Vic West" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // JAMES BAY - COUNTERCLOCKWISE
				if (Arrays.asList( //
						"James Bay - To 3 R. Jubilee", //
						"To Vic West Only" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.COUNTERCLOCKWISE);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 11L) {
			if (gTrip.getDirectionId() == 0) { // TILLICUM MALL - WEST
				if (Arrays.asList( //
						"Tillicum Mall Via Gorge" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - EAST
				if (Arrays.asList( //
						"Downtown", //
						"UVic Via Uplands" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 12L) {
			if (gTrip.getDirectionId() == 0) { // UNIVERSITY HGTS - WEST
				if (Arrays.asList( //
						"University Hgts Via Kenmore" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - EAST
				if (Arrays.asList( //
						"UVic Via Kenmore" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 13L) {
			if (gTrip.getDirectionId() == 0) { // UVIC - WEST
				if (Arrays.asList( //
						"UVic" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // TEN MILE POINT - EAST
				if (Arrays.asList( //
						"Ten Mile Point" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 14L) {
			if (gTrip.getDirectionId() == 0) { // VIC GENERAL - WEST
				if (Arrays.asList( //
						"Downtown", //
						"Vic General Via Craigflower" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - EAST
				if (Arrays.asList( //
						"Downtown", //
						"UVic", //
						"UVic Via Richmond" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 15L) {
			if (gTrip.getDirectionId() == 0) { // ESQUIMALT - WEST
				if (Arrays.asList( //
						"Esquimalt", //
						"Esquimalt - Fort/Yates Exp" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - EAST
				if (Arrays.asList( //
						"Downtown", //
						"UVic - Foul Bay Exp" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 16L) {
			if (gTrip.getDirectionId() == 0) { // UPTOWN - WEST
				if (Arrays.asList( //
						"Uptown - McKenzie Exp" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - EAST
				if (Arrays.asList( //
						"UVic - McKenzie Exp" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 17L) {
			if (gTrip.getDirectionId() == 0) { // Downtown - WEST
				if (Arrays.asList( //
						"Downtown Via Quadra" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - EAST
				if (Arrays.asList( //
						"UVic Via Cedar Hill Sch" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 21L) {
			if (gTrip.getDirectionId() == 0) { // INTERURBAN - CLOCKWISE
				if (Arrays.asList( //
						"Interurban - VI Tech Park", //
						"Interurban - Camosun Only", //
						"Interurban - Viaduct Loop", //
						"21N Camosun Via Burnside" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - COUNTERCLOCKWISE
				if (Arrays.asList( //
						"Downtown To 7 UVic" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.COUNTERCLOCKWISE);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 22L) {
			if (gTrip.getDirectionId() == 0) { // VIC GENERAL - NORTH
				if (Arrays.asList( //
						"Downtown", //
						"Vic General - Watkiss Way Via Burnside", //
						"22A Vic General - Watkiss Wy Via S. Vale", //
						"To Spectrum School", //
						"Vic General Via Burnside" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // NILLSIDE MALL - SOUTH
				if (Arrays.asList( //
						"Downtown", //
						"22A Hillside Mall Via Straw Vale", //
						"Hillside Mall Via Fernwood" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 24L) {
			if (gTrip.getDirectionId() == 0) { // Admirals Walk - WEST
				if (Arrays.asList( //
						"Downtown", //
						"Admirals Walk Via Parklands/Colville", //
						"Admirals Walk Via Colville" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // Cedar Hill - EAST
				if (Arrays.asList( //
						"Cedar Hill", //
						"Cedar Hill Via Parklands" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 25L) {
			if (gTrip.getDirectionId() == 0) { // Admirals Walk - WEST
				if (Arrays.asList( //
						"Shoreline Sch Via Munro", //
						"Admirals Walk Via Munro" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // Maplewood - EAST
				if (Arrays.asList( //
						"Maplewood" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 26L) {
			if (gTrip.getDirectionId() == 0) { // DOCKYARD - WEST
				if (Arrays.asList( //
						"To Uptown Only", //
						"Dockyard Via McKenzie" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - EAST
				if (Arrays.asList( //
						"To Uptown Only", //
						"UVic Via McKenzie" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 27L) {
			if (gTrip.getDirectionId() == 0) { // GORDON HEAD - NORTH
				if (Arrays.asList( //
						"Gordon Head Via Shelbourne" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - SOUTH
				if (Arrays.asList( //
						"27X Express To Downtown", //
						"To Hillside Only", //
						"Downtown" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 28L) {
			if (gTrip.getDirectionId() == 0) { // MAJESTIC - NORTH
				if (Arrays.asList( //
						"28X Express To Majestic", //
						"Majestic Via Shelbourne" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - SOUTH
				if (Arrays.asList( //
						"To McKenzie Only", //
						"To Hillside Only", //
						"Downtown" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 30L) {
			if (Arrays.asList( //
					"Royal Oak Exch Via Carey", //
					"Royal Oak Exch To 75 Saanichton" //
			).contains(gTrip.getTripHeadsign())) {
				mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.NORTH);
				return;
			}
			if (Arrays.asList( //
					"Downtown" //
			).contains(gTrip.getTripHeadsign())) {
				mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.SOUTH);
				return;
			}
		} else if (mTrip.getRouteId() == 31L) {
			if (gTrip.getDirectionId() == 0) { // ROYAL OAK - NORTH
				if (Arrays.asList( //
						"Royal Oak Exch To 75 Saanichton", //
						"Royal Oak Exch Via Glanford" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - SOUTH
				if (Arrays.asList( //
						"To Gorge Only", //
						"To Uptown Only", //
						"Downtown" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 32L) {
			if (gTrip.getDirectionId() == 0) { // Cordova Bay - NORTH
				if (Arrays.asList( //
						"Cordova Bay" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // ROYAL OAK - SOUTH
				if (Arrays.asList( //
						"Downtown", //
						"Royal Oak Exch" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 35L) {
			// TODO split? NORTH/SOUTH
			if (gTrip.getDirectionId() == 0) { // Ridge - CLOCKWISE
				if (Arrays.asList( //
						"Ridge" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 39L) {
			if (gTrip.getDirectionId() == 0) { // WESTHILLS - WEST
				if (Arrays.asList( //
						"Royal Oak Exch", //
						"Interurban", //
						"Westhills Exch" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - EAST
				if (Arrays.asList( //
						"UVic Via Royal Oak" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 43L) {
			if (gTrip.getDirectionId() == 0) { // ROYAL ROADS - CLOCKWISE
				if (Arrays.asList( //
						"Belmont Park - Royal Roads" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 46L) {
			if (gTrip.getDirectionId() == 0) { // WESTHILLS - WEST
				if (Arrays.asList( //
						"Westhills Exch" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOCKYARD - EAST
				if (Arrays.asList( //
						"Dockyard" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 47L) {
			if (gTrip.getDirectionId() == 0) { // GOLDSTREAM MEADOWS - WEST
				if (Arrays.asList( //
						"Goldstream Mdws Via Thetis Hgts" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - EAST
				if (Arrays.asList( //
						"Downtown" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 48L) {
			if (gTrip.getDirectionId() == 0) { // HAPPY VALLEY - WEST
				if (Arrays.asList( //
						"Happy Valley via Colwood", //
						"HAPPY VALLEY VIA COLWOOD" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - EAST
				if (Arrays.asList( //
						"Downtown" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 50L) {
			if (gTrip.getDirectionId() == 0) { // LANGFORD - WEST
				if (Arrays.asList( //
						"Langford To 61 Sooke", //
						"Langford" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - EAST
				if (Arrays.asList( //
						"Downtown" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 51L) {
			if (gTrip.getDirectionId() == 0) { // LANGFORD - WEST
				if (Arrays.asList( //
						"Langford - McKenzie Exp" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - EAST
				if (Arrays.asList( //
						"UVic - McKenzie Exp" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 52L) {
			if (gTrip.getDirectionId() == 0) { // BEAR MOUNTAIN - WEST
				if (Arrays.asList( //
						"Langford Exch Via Royal Bay", //
						"Langford Exch Via Lagoon", //
						"Langford Exch", //
						"Bear Mountain - Lagoon/Royal Bay", //
						"Bear Mountain Via Lagoon", //
						"Bear Mountain" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // COLWOOD EXCHANGE - EAST
				if (Arrays.asList( //
						"Langford Exch", //
						"Colwood Exch Via Royal Bay/Lagoon", //
						"Colwood Exch Via Royal Bay", //
						"Colwood Exch Via Lagoon", //
						"Colwood Exch" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 53L) {
			if (gTrip.getDirectionId() == 0) { // COLWOOD EXCHANGE - CLOCKWISE
				if (Arrays.asList( //
						"Colwood Exch Via Atkins" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // LANGFORD EXCHANGE - COUNTERCLOCKWISE
				if (Arrays.asList( //
						"Langford Exch Via Atkins" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.COUNTERCLOCKWISE);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 54L) {
			if (gTrip.getDirectionId() == 0) { // LANGFORD EXCHANGE - CLOCKWISE
				if (Arrays.asList( //
						"Metchosin" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 55L) {
			if (gTrip.getDirectionId() == 1) { // LANGFORD EXCHANGE - COUNTERCLOCKWISE
				if (Arrays.asList( //
						"Happy Valley To Colwood Exch", //
						"Happy Valley" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.COUNTERCLOCKWISE);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 56L) {
			if (gTrip.getDirectionId() == 0) { // THETIS HEIGHTS - NORTH
				if (Arrays.asList( //
						"Thetis Heights Via Florence Lake" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // LANGFORD EXCHANGE - SOUTH
				if (Arrays.asList( //
						"Langford Exch" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 57L) {
			if (gTrip.getDirectionId() == 0) { // THETIS HEIGHTS - NORTH
				if (Arrays.asList( //
						"Theits Heights Via Millstream" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // LANGFORD EXCHANGE - SOUTH
				if (Arrays.asList( //
						"Langford Exch" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 58L) {
			if (gTrip.getDirectionId() == 1) { // GOLDSTREAM MEADOWS - OUTBOUND
				if (Arrays.asList( //
						"Goldstream Mdws" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.OUTBOUND);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 59L) {
			if (gTrip.getDirectionId() == 1) { // LANGFORD EXCHANGE - COUNTERCLOCKWISE
				if (Arrays.asList( //
						"Triangle Mtn Via Royal Bay", //
						"Triangle Mtn" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.COUNTERCLOCKWISE);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 60L) {
			if (gTrip.getDirectionId() == 0) { // LANGFORD EXCHANGE - CLOCKWISE
				if (Arrays.asList( //
						"Wishart Via Royal Bay", //
						"Wishart" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 61L) {
			if (gTrip.getDirectionId() == 0) { // SOOKE - WEST
				if (Arrays.asList( //
						"Sooke" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - EAST
				if (Arrays.asList( //
						"Langford - Jacklin/Station", //
						"Langford Exch To 50 Downtown", //
						"Downtown" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 63L) {
			// TODO split?
			if (gTrip.getDirectionId() == 0) { // OTTER POINT - WEST
				if (Arrays.asList( //
						"Otter Point" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 64L) {
			// TODO split
			if (gTrip.getDirectionId() == 0) { // SOOKE - CLOCKWISE
				if (Arrays.asList( //
						"East Sooke To 17 Mile House", //
						"East Sooke To Langford", //
						"East Sooke To Sooke" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			}
			if (isGoodEnoughAccepted()) {
				if (gTrip.getDirectionId() == 1) { // SOOKE - ????
					if (Arrays.asList( //
							"East Sooke" //
					).contains(gTrip.getTripHeadsign())) {
						mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.COUNTERCLOCKWISE);
						return;
					}
				}
			}
		} else if (mTrip.getRouteId() == 65L) {
			if (gTrip.getDirectionId() == 0) { // SOOKE - WEST
				if (Arrays.asList( //
						"Sooke Via Westhills" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - EAST
				if (Arrays.asList( //
						"Downtown Via Westhills" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 70L) {
			if (gTrip.getDirectionId() == 0) { // SWARTZ BAY FERRY - NORTH
				if (Arrays.asList( //
						"Swartz Bay Ferry Via Hwy #17" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - SOUTH
				if (Arrays.asList( //
						"To Gorge Only Via Hwy #17", //
						"Downtown Via Hwy #17" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 71L) {
			if (gTrip.getDirectionId() == 0) { // SWARTZ BAY FERRY - NORTH
				if (Arrays.asList( //
						"Swartz Bay Ferry Via West Sidney" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - SOUTH
				if (Arrays.asList( //
						"Downtown" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 72L) {
			if (gTrip.getDirectionId() == 0) { // SWARTZ BAY FERRY - NORTH
				if (Arrays.asList( //
						"McDonald Park Via Saanichton", //
						"Swartz Bay Ferry Via Saanichton" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - SOUTH
				if (Arrays.asList( //
						"McTavish Exch", //
						"Downtown" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 75L) {
			if (gTrip.getDirectionId() == 0) { // SAANICHTON - NORTH
				if (Arrays.asList( //
						"To Keating Only", //
						"Saanichton Exch Via Verdier", //
						"Saanichton Exch" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - SOUTH
				if (Arrays.asList( //
						"Royal Oak Exch To 30 Downtown", //
						"Royal Oak Exch To 31 Downtown", //
						"Downtown" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 76L) {
			if (isGoodEnoughAccepted()) { // TODO check
				if (gTrip.getDirectionId() == 0) { // SWARTZ BAY FERRY - NORTH
					if (Arrays.asList( //
							"Swartz Bay Ferry Non-Stop" //
					).contains(gTrip.getTripHeadsign())) {
						mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.NORTH);
						return;
					}
				} else if (gTrip.getDirectionId() == 1) { // UVIC - SOUTH
					if (Arrays.asList( //
							"UVic - Via Express" //
					).contains(gTrip.getTripHeadsign())) {
						mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.SOUTH);
						return;
					}
				}
			}
		} else if (mTrip.getRouteId() == 81L) {
			if (gTrip.getDirectionId() == 0) { // SWARTZ BAY FERRY - NORTH
				if (Arrays.asList( //
						"To Sidney Only", //
						"Swartz Bay Ferry" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // BRENTWOOD - SOUTH
				if (Arrays.asList( //
						"Saanichton Exch", //
						"Brentwood To Verdier Only", //
						"Brentwood" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 82L) {
			if (gTrip.getDirectionId() == 0) { // SIDNEY - NORTH
				if (Arrays.asList( //
						"Sidney Via Stautw" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // SAANICHTON - SOUTH
				if (Arrays.asList( //
						"To Brentwood Via Stautw", //
						"Saanichton Exch Via Stautw" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 83L) {
			if (gTrip.getDirectionId() == 0) { // SIDNEY - NORTH
				if (Arrays.asList( //
						"Sidney Via West Saanich" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // ROYAL OAK - SOUTH
				if (Arrays.asList( //
						"Royal Oak Exch Via West Saanich" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 85L) {
			// TODO split
			if (gTrip.getDirectionId() == 0) { // NORTH SAANICH - CLOCKWISE
				if (Arrays.asList( //
						"North Saanich" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // NORTH SAANICH - CLOCKWISE
				if (Arrays.asList( //
						"North Saanich" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 87L) {
			if (gTrip.getDirectionId() == 0) { // SIDNEY - NORTH
				if (Arrays.asList( //
						"Sidney" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // SAANICHTON - SOUTH
				if (Arrays.asList( //
						"Dean Park Via Airport To Saanichton" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (mTrip.getRouteId() == 88L) {
			if (gTrip.getDirectionId() == 0) { // SIDNEY - NORTH
				if (Arrays.asList( //
						"Sidney" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // AIRPORT - SOUTH
				if (Arrays.asList( //
						"Airport" //
				).contains(gTrip.getTripHeadsign())) {
					mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		}
		System.out.printf("\n%s: Unexpected trips headsign for %s!\n", mTrip.getRouteId(), gTrip);
		System.exit(-1);
	}

	@Override
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		List<String> headsignsValues = Arrays.asList(mTrip.getHeadsignValue(), mTripToMerge.getHeadsignValue());
		if (mTrip.getRouteId() == 2L) {
			if (Arrays.asList( //
					JAMES_BAY, // <>
					DOWNTOWN, // <>
					WILLOWS, //
					SOUTH_OAK_BAY //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SOUTH_OAK_BAY, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 3L) {
			if (Arrays.asList( //
					DOWNTOWN, // <>
					R_JUBILEE, //
					JAMES_BAY // ++
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(JAMES_BAY, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 4L) {
			if (Arrays.asList( //
					GORGE + AND + DOUGLAS, //
					DOWNTOWN //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 6L) {
			if (Arrays.asList( //
					"A " + DOWNTOWN, //
					"B " + DOWNTOWN, //
					DOWNTOWN //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"A " + ROYAL_OAK_EXCH, //
					"B " + ROYAL_OAK_EXCH, //
					ROYAL_OAK_EXCH //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ROYAL_OAK_EXCH, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 7L) {
			if (Arrays.asList( //
					"N " + U_VIC, //
					U_VIC //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(U_VIC, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					INTERURBAN, //
					"N " + DOWNTOWN, //
					DOWNTOWN //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 8L) {
			if (Arrays.asList( //
					DOUGLAS, //
					RICHMOND + AND + OAK_BAY + " Ave", //
					OAK_BAY //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(OAK_BAY, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					TILLICUM_MALL, //
					INTERURBAN //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(INTERURBAN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 10L) {
			if (Arrays.asList( //
					VIC_WEST, //
					JAMES_BAY //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(JAMES_BAY, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 11L) {
			if (Arrays.asList( //
					DOWNTOWN, //
					U_VIC //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(U_VIC, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 14L) {
			if (Arrays.asList( //
					DOWNTOWN, // <>
					U_VIC //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(U_VIC, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					DOWNTOWN, // <>
					VIC_GENERAL //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(VIC_GENERAL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 15L) {
			if (Arrays.asList( //
					DOWNTOWN, //
					U_VIC //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(U_VIC, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 21L) {
			if (Arrays.asList( //
					"N " + CAMOSUN, //
					INTERURBAN //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(INTERURBAN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 22L) {
			if (Arrays.asList( //
					DOWNTOWN, // <>
					"A " + VIC_GENERAL, //
					SPECTRUM_SCHOOL, //
					VIC_GENERAL //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(VIC_GENERAL, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					DOWNTOWN, // <>
					"A " + HILLSIDE_MALL, //
					HILLSIDE_MALL //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(HILLSIDE_MALL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 24L) {
			if (Arrays.asList( //
					DOWNTOWN, //
					ADMIRALS_WALK //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ADMIRALS_WALK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 25L) {
			if (Arrays.asList( //
					SHORELINE_SCHOOL, //
					ADMIRALS_WALK //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ADMIRALS_WALK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 26L) {
			if (Arrays.asList( //
					UPTOWN, // <>
					U_VIC //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(U_VIC, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					UPTOWN, // <>
					DOCKYARD //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOCKYARD, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 27L) {
			if (Arrays.asList( //
					HILLSIDE, //
					DOWNTOWN //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 28L) {
			if (Arrays.asList( //
					MC_KENZIE, //
					HILLSIDE, //
					DOWNTOWN //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 30L) {
			if (Arrays.asList( //
					SAANICHTON, //
					ROYAL_OAK_EXCH //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ROYAL_OAK_EXCH, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 31L) {
			if (Arrays.asList( //
					SAANICHTON, //
					ROYAL_OAK_EXCH //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ROYAL_OAK_EXCH, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					GORGE, //
					UPTOWN, //
					DOWNTOWN //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 32L) {
			if (Arrays.asList( //
					DOWNTOWN, //
					ROYAL_OAK_EXCH //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ROYAL_OAK_EXCH, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 39L) {
			if (Arrays.asList( //
					ROYAL_OAK_EXCH, //
					INTERURBAN, //
					WESTHILLS_EXCH //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(WESTHILLS_EXCH, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 50L) {
			if (Arrays.asList( //
					LANGFORD, //
					SOOKE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(LANGFORD, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 52L) {
			if (Arrays.asList( //
					LANGFORD_EXCH, // <>
					COLWOOD_EXCH //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(COLWOOD_EXCH, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					LANGFORD_EXCH, // <>
					BEAR_MOUTAIN //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(BEAR_MOUTAIN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 55L) {
			if (Arrays.asList( //
					COLWOOD_EXCH, //
					HAPPY_VLY //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(HAPPY_VLY, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 61L) {
			if (Arrays.asList( //
					LANGFORD, //
					DOWNTOWN //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 64L) {
			if (Arrays.asList( //
					MILE_HOUSE, //
					LANGFORD, //
					SOOKE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SOOKE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 70L) {
			if (Arrays.asList( //
					GORGE, //
					DOWNTOWN //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 72L) {
			if (Arrays.asList( //
					MC_DONALD_PARK, //
					SWARTZ_BAY_FERRY //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SWARTZ_BAY_FERRY, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					MC_TAVISH_EXCH, //
					DOWNTOWN //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 75L) {
			if (Arrays.asList( //
					KEATING, //
					SAANICHTON_EXCH //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SAANICHTON_EXCH, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					ROYAL_OAK_EXCH, //
					DOWNTOWN //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 81L) {
			if (Arrays.asList( //
					SAANICHTON_EXCH, //
					VERDIER, //
					BRENTWOOD //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(BRENTWOOD, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					SIDNEY, //
					SWARTZ_BAY_FERRY //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SWARTZ_BAY_FERRY, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 82L) {
			if (Arrays.asList( //
					SAANICHTON_EXCH, //
					BRENTWOOD //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(BRENTWOOD, mTrip.getHeadsignId());
				return true;
			}
		}
		System.out.printf("\nUnexpected trips to merges %s & %s!\n", mTrip, mTripToMerge);
		System.exit(-1);
		return false;
	}

	private static final Pattern EXCHANGE = Pattern.compile("((^|\\W){1}(exchange)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String EXCHANGE_REPLACEMENT = "$2" + EXCH + "$4";

	private static final Pattern HEIGHTS = Pattern.compile("((^|\\W){1}(Hghts)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String HEIGHTS_REPLACEMENT = "$2Hts$4";

	private static final Pattern STARTS_WITH_NUMBER = Pattern.compile("(^[\\d]+)", Pattern.CASE_INSENSITIVE);

	private static final Pattern ENDS_WITH_EXPRESS = Pattern.compile("( express.*$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern ENDS_WITH_VIA = Pattern.compile("( via .*$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_TO = Pattern.compile("(^.* to )", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_TO_ = Pattern.compile("(^to )", Pattern.CASE_INSENSITIVE);

	private static final Pattern ENDS_WITH_DASH = Pattern.compile("( \\- .*$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern ENDS_WITH_NON_STOP = Pattern.compile("( non\\-stop$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern ENDS_WITH_ONLY = Pattern.compile("( only$)", Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		if (Utils.isUppercaseOnly(tripHeadsign, true, true)) {
			tripHeadsign = tripHeadsign.toLowerCase(Locale.ENGLISH);
		}
		tripHeadsign = EXCHANGE.matcher(tripHeadsign).replaceAll(EXCHANGE_REPLACEMENT);
		tripHeadsign = HEIGHTS.matcher(tripHeadsign).replaceAll(HEIGHTS_REPLACEMENT);
		tripHeadsign = ENDS_WITH_DASH.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = ENDS_WITH_VIA.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = STARTS_WITH_TO.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = ENDS_WITH_EXPRESS.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = STARTS_WITH_NUMBER.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = STARTS_WITH_TO_.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = ENDS_WITH_NON_STOP.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = ENDS_WITH_ONLY.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = CleanUtils.cleanSlashes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern STARTS_WITH_BOUND = Pattern.compile("(^(east|west|north|south)bound)", Pattern.CASE_INSENSITIVE);

	private static final Pattern UVIC = Pattern.compile("((^|\\W){1}(uvic)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String UVIC_REPLACEMENT = "$2" + U_VIC + "$4";

	private static final Pattern STARTS_WITH_IMPL = Pattern.compile("(^(\\(\\-IMPL\\-\\)))", Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = STARTS_WITH_IMPL.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		gStopName = STARTS_WITH_BOUND.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.CLEAN_AND.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		gStopName = EXCHANGE.matcher(gStopName).replaceAll(EXCHANGE_REPLACEMENT);
		gStopName = UVIC.matcher(gStopName).replaceAll(UVIC_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@Override
	public int getStopId(GStop gStop) {
		return Integer.parseInt(gStop.getStopCode()); // use stop code as stop ID
	}
}
