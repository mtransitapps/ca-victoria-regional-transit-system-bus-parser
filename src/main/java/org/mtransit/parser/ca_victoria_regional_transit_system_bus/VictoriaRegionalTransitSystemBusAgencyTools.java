package org.mtransit.parser.ca_victoria_regional_transit_system_bus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.StrategicMappingCommons;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.StringUtils;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.mtransit.parser.Constants.EMPTY;

// https://www.bctransit.com/open-data
// https://victoria.mapstrat.com/current/google_transit.zip
public class VictoriaRegionalTransitSystemBusAgencyTools extends DefaultAgencyTools {

	public static void main(@Nullable String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-victoria-regional-transit-system-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new VictoriaRegionalTransitSystemBusAgencyTools().start(args);
	}

	@Nullable
	private HashSet<Integer> serviceIdInts;

	@Override
	public void start(@NotNull String[] args) {
		MTLog.log("Generating Victoria Regional TS bus data...");
		long start = System.currentTimeMillis();
		this.serviceIdInts = extractUsefulServiceIdInts(args, this, true);
		super.start(args);
		MTLog.log("Generating Victoria Regional TS bus data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIdInts != null && this.serviceIdInts.isEmpty();
	}

	@Override
	public boolean excludeCalendar(@NotNull GCalendar gCalendar) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarInt(gCalendar, this.serviceIdInts);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(@NotNull GCalendarDate gCalendarDates) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarDateInt(gCalendarDates, this.serviceIdInts);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(@NotNull GTrip gTrip) {
		if (this.serviceIdInts != null) {
			return excludeUselessTripInt(gTrip, this.serviceIdInts);
		}
		return super.excludeTrip(gTrip);
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public long getRouteId(@NotNull GRoute gRoute) { // used by GTFS-RT
		//noinspection deprecation
		final String routeId = gRoute.getRouteId();
		if (!Utils.isDigitsOnly(routeId)) {
			if ("ARB".equalsIgnoreCase(gRoute.getRouteShortName())) {
				return 999L;
			}
			return Long.parseLong(gRoute.getRouteShortName()); // use route short name as route ID
		}
		return super.getRouteId(gRoute); // used by GTFS-RT
	}

	@NotNull
	@Override
	public String getRouteLongName(@NotNull GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongNameOrDefault();
		routeLongName = CleanUtils.cleanNumbers(routeLongName);
		routeLongName = CleanUtils.cleanStreetTypes(routeLongName);
		return CleanUtils.cleanLabel(routeLongName);
	}

	private static final String AGENCY_COLOR_GREEN = "34B233";// GREEN (from PDF Corporate Graphic Standards)
	private static final String AGENCY_COLOR_BLUE = "002C77"; // BLUE (from PDF Corporate Graphic Standards)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Nullable
	@Override
	public String getRouteColor(@NotNull GRoute gRoute) {
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
	private static final String HILLSIDE_CENTER = HILLSIDE + " Ctr";
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
	private static final String TILLICUM = "Tillicum";
	private static final String TILLICUM_CENTER = TILLICUM + " Ctr";
	private static final String TILLICUM_MALL = TILLICUM + " Mall";
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
	private static final String R_JUBILEE = "R Jubilee";
	private static final String VIC_WEST = "Vic West";

	private final HashMap<Long, Long> routeIdToShortName = new HashMap<>();

	@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
	@Override
	public void setTripHeadsign(@NotNull MRoute mRoute, @NotNull MTrip mTrip, @NotNull GTrip gTrip, @NotNull GSpec gtfs) {
		final long rsn = Long.parseLong(mRoute.getShortName());
		this.routeIdToShortName.put(mRoute.getId(), rsn);
		String tripHeadsign = gTrip.getTripHeadsign();
		tripHeadsign = Pattern.compile("(^" + mRoute.getShortName() + "( )?)", Pattern.CASE_INSENSITIVE).matcher(tripHeadsign).replaceAll(EMPTY);
		if (rsn == 1L) {
			if (gTrip.getDirectionId() == 0) { // DOWNTOWN - WEST
				if (Arrays.asList( //
						"Downtown" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // SOUTH OAK BAY - EAST
				if (Arrays.asList( //
						"South Oak Bay via Richardson" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 2L) {
			if (gTrip.getDirectionId() == 0) { // JAMES BAY - WEST
				if (Arrays.asList( //
						"James Bay - Fisherman's Wharf" // <>
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // OAK BAY - EAST
				if (Arrays.asList( //
						"James Bay - Fisherman's Wharf", // <>
						"Downtown", //
						"Downtown Only", //
						"South Oak Bay - Oak Bay Village", //
						"Willows - Oak Bay Village" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 3L) {
			if (gTrip.getDirectionId() == 0) { // JAMES BAY - CLOCKWISE
				if (Arrays.asList( //
						"Downtown Only", //
						"James Bay To 10 R. Jubilee", //
						"James Bay - Linden to 10 R. Jubilee", //
						"James Bay - Quimper To 10 R. Jubilee" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // ROYAL JUBILEE - COUNTERCLOCKWISE
				if (Arrays.asList( //
						"Royal Jubilee - Cook St Village", //
						"Royal Jubilee - Cook St Vlg/Quimper" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.COUNTERCLOCKWISE);
					return;
				}
			}
		} else if (rsn == 4L) {
			if (gTrip.getDirectionId() == 0) { // DOWNTOWN - WEST
				if (Arrays.asList( //
						"Downtown", //
						"To Gorge & Douglas" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - EAST
				if (Arrays.asList( //
						"UVic Via Hillside" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 6L) {
			if (gTrip.getDirectionId() == 0) { // ROYAL OAK - NORTH
				if (Arrays.asList( //
						"Royal Oak Exch Via Royal Oak Mall", //
						"Royal Oak Exch Via Royal Oak Ctr", //
						"A Royal Oak Exch Via Emily Carr", //
						"B Royal Oak Exch Via Chatterton" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - SOUTH
				if (Arrays.asList( //
						"Downtown", //
						"B Downtown Via Chatterton", //
						"A Downtown Via Emily Carr" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (rsn == 7L) {
			if (gTrip.getDirectionId() == 0) { // DOWNTOWN - CLOCKWISE
				if (Arrays.asList( //
						"Downtown Only", //
						"N Downtown Only", //
						"Downtown - To 21 Interurban", //
						"N Downtown - To 21 Interurban", //
						"Downtown To 21 Interurban" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - COUNTERCLOCKWISE
				if (Arrays.asList( //
						"UVic Via Fairfield", //
						"N UVic - Cook St Village" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.COUNTERCLOCKWISE);
					return;
				}
			}
		} else if (rsn == 8L) {
			if (gTrip.getDirectionId() == 0) { // INTERURBAN - WEST
				if (Arrays.asList( //
						"Tillicum Ctr Via Finalyson", //
						"Tillicum Mall Via Finalyson", //
						"Interurban Via Finlayson" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // OAK BAY - EAST
				if (Arrays.asList( //
						"To Richmond & Oak Bay Ave Only", //
						"To Douglas Only - Mayfair Mall", //
						"Oak Bay Via Finlayson", //
						"Oak Bay Via Finalyson" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 9L) {
			if (gTrip.getDirectionId() == 0) { // ROYAL OAK - WEST
				if (Arrays.asList( //
						"Royal Oak Exch - Hillside/Gorge" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - EAST
				if (Arrays.asList( //
						"UVic - Gorge/Hillside" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 10L) {
			if (gTrip.getDirectionId() == 0) { // ROYAL JUBILEE - CLOCKWISE
				if (Arrays.asList( //
						"Royal Jubilee Via Vic West" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // JAMES BAY - COUNTERCLOCKWISE
				if (Arrays.asList( //
						"James Bay - To 3 R. Jubilee", //
						"To Vic West Only" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.COUNTERCLOCKWISE);
					return;
				}
			}
		} else if (rsn == 11L) {
			if (gTrip.getDirectionId() == 0) { // TILLICUM MALL - WEST
				if (Arrays.asList( //
						"Tillicum Ctr Via Gorge", //
						"Tillicum Mall Via Gorge" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - EAST
				if (Arrays.asList( //
						"Downtown", //
						"Downtown Only", //
						"UVic Via Uplands" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 12L) {
			if (gTrip.getDirectionId() == 0) { // UNIVERSITY HGTS - WEST
				if (Arrays.asList( //
						"University Hgts Via Kenmore" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - EAST
				if (Arrays.asList( //
						"UVic Via Kenmore" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 13L) {
			if (gTrip.getDirectionId() == 0) { // UVIC - WEST
				if (Arrays.asList( //
						"UVic" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // TEN MILE POINT - EAST
				if (Arrays.asList( //
						"Ten Mile Point" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 14L) {
			if (gTrip.getDirectionId() == 0) { // VIC GENERAL - WEST
				if (Arrays.asList( //
						"Downtown Only", //
						"Downtown", //
						"Vic General Via Craigflower" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - EAST
				if (Arrays.asList( //
						"Downtown Only", //
						"Downtown", //
						"UVic", //
						"UVic Via Richmond" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 15L) {
			if (gTrip.getDirectionId() == 0) { // ESQUIMALT - WEST
				if (Arrays.asList( //
						"Esquimalt", //
						"Esquimalt - Fort/Yates Exp" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - EAST
				if (Arrays.asList( //
						"Downtown Only", //
						"Downtown", //
						"UVic - Foul Bay Exp" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 16L) {
			if (gTrip.getDirectionId() == 0) { // UPTOWN - WEST
				if (Arrays.asList( //
						"Uptown - McKenzie Exp" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - EAST
				if (Arrays.asList( //
						"UVic - McKenzie Exp" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 17L) {
			if (gTrip.getDirectionId() == 0) { // Downtown - WEST
				if (Arrays.asList( //
						"Downtown Via Quadra" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - EAST
				if (Arrays.asList( //
						"UVic Via Cedar Hill Sch" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 21L) {
			if (gTrip.getDirectionId() == 0) { // INTERURBAN - CLOCKWISE
				if (Arrays.asList( //
						"Interurban - VI Tech Park", //
						"Interurban - Camosun Only", //
						"Interurban - Viaduct Loop", //
						"N Camosun Via Burnside" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - COUNTERCLOCKWISE
				if (Arrays.asList( //
						"Downtown To 7 UVic" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.COUNTERCLOCKWISE);
					return;
				}
			}
		} else if (rsn == 22L) {
			if (gTrip.getDirectionId() == 0) { // VIC GENERAL - NORTH
				if (Arrays.asList( //
						"Downtown Only", //
						"Downtown", //
						"Vic General - Watkiss Way Via Burnside", //
						"A Vic General - Watkiss Wy Via S. Vale", //
						"A Vic General Via Straw Vale", //
						"A Vic General Via S. Vale", //
						"To Spectrum School", //
						"Vic General Via Burnside" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // NILLSIDE MALL - SOUTH
				if (Arrays.asList( //
						"Downtown Only", //
						"Downtown", //
						"A Hillside Mall Via Straw Vale", //
						"A Hillside Ctr Via Straw Vale", //
						"Hillside Ctr Via Fernwood", //
						"Hillside Mall Via Fernwood" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (rsn == 24L) {
			if (gTrip.getDirectionId() == 0) { // Admirals Walk - WEST
				if (Arrays.asList( //
						"Downtown Only", //
						"Downtown", //
						"Admirals Walk Via Parklands/Colville", //
						"Admirals Walk Via Colville" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // Cedar Hill - EAST
				if (Arrays.asList( //
						"Cedar Hill", //
						"Cedar Hill Via Parklands" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 25L) {
			if (gTrip.getDirectionId() == 0) { // Admirals Walk - WEST
				if (Arrays.asList( //
						"Shoreline Sch Via Munro", //
						"Admirals Walk Via Munro" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // Maplewood - EAST
				if (Arrays.asList( //
						"Maplewood" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 26L) {
			if (gTrip.getDirectionId() == 0) { // DOCKYARD - WEST
				if (Arrays.asList( //
						"To Uptown Only", //
						"Dockyard Via McKenzie" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - EAST
				if (Arrays.asList( //
						"To Uptown Only", //
						"UVic Via McKenzie" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 27L) {
			if (gTrip.getDirectionId() == 0) { // GORDON HEAD - NORTH
				if (Arrays.asList( //
						"Gordon Head Via Shelbourne" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - SOUTH
				if (Arrays.asList( //
						"X Express To Downtown", //
						"To Hillside Only", //
						"Downtown" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (rsn == 28L) {
			if (gTrip.getDirectionId() == 0) { // MAJESTIC - NORTH
				if (Arrays.asList( //
						"X Express To Majestic", //
						"Majestic Via Shelbourne" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - SOUTH
				if (Arrays.asList( //
						"To McKenzie Only", //
						"To Hillside Only", //
						"Downtown" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (rsn == 30L) {
			if (Arrays.asList( //
					"Royal Oak Exch Via Carey", //
					"Royal Oak Exch To 75 Saanichton" //
			).contains(tripHeadsign)) {
				mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.NORTH);
				return;
			}
			if (Arrays.asList( //
					"Downtown" //
			).contains(tripHeadsign)) {
				mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.SOUTH);
				return;
			}
		} else if (rsn == 31L) {
			if (gTrip.getDirectionId() == 0) { // ROYAL OAK - NORTH
				if (Arrays.asList( //
						"Royal Oak Exch To 75 Saanichton", //
						"Royal Oak Exch Via Glanford" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - SOUTH
				if (Arrays.asList( //
						"To Gorge Only", //
						"To Uptown Only", //
						"Downtown" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (rsn == 32L) {
			if (gTrip.getDirectionId() == 0) { // Cordova Bay - NORTH
				if (Arrays.asList( //
						"Cordova Bay" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // ROYAL OAK - SOUTH
				if (Arrays.asList( //
						"Downtown", //
						"Royal Oak Exch" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (rsn == 35L) {
			// TODO split? NORTH/SOUTH
			if (gTrip.getDirectionId() == 0) { // Ridge - CLOCKWISE
				if (Arrays.asList( //
						"Ridge" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			}
		} else if (rsn == 39L) {
			if (gTrip.getDirectionId() == 0) { // WESTHILLS - WEST
				if (Arrays.asList( //
						"Royal Oak Exch", //
						"Interurban", //
						"Westhills Exch" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - EAST
				if (Arrays.asList( //
						"UVic Via Royal Oak" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 43L) {
			if (gTrip.getDirectionId() == 0) { // ROYAL ROADS - CLOCKWISE
				if (Arrays.asList( //
						"Belmont Park - Royal Roads" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			}
		} else if (rsn == 46L) {
			if (gTrip.getDirectionId() == 0) { // WESTHILLS - WEST
				if (Arrays.asList( //
						"Westhills Exch" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOCKYARD - EAST
				if (Arrays.asList( //
						"Dockyard" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 47L) {
			if (gTrip.getDirectionId() == 0) { // GOLDSTREAM MEADOWS - WEST
				if (Arrays.asList( //
						"Goldstream Mdws Via Thetis Hgts" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - EAST
				if (Arrays.asList( //
						"Downtown" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 48L) {
			if (gTrip.getDirectionId() == 0) { // HAPPY VALLEY - WEST
				if (Arrays.asList( //
						"Happy Valley via Colwood", //
						"HAPPY VALLEY VIA COLWOOD" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - EAST
				if (Arrays.asList( //
						"Downtown" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 50L) {
			if (gTrip.getDirectionId() == 0) { // LANGFORD - WEST
				if (Arrays.asList( //
						"Langford To 61 Sooke", //
						"Langford" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - EAST
				if (Arrays.asList( //
						"Downtown" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 51L) {
			if (gTrip.getDirectionId() == 0) { // LANGFORD - WEST
				if (Arrays.asList( //
						"Langford - McKenzie Exp" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - EAST
				if (Arrays.asList( //
						"UVic - McKenzie Exp" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 52L) {
			if (gTrip.getDirectionId() == 0) { // BEAR MOUNTAIN - WEST
				if (Arrays.asList( //
						"To Royal Bay Sch Via Lagoon", //
						"Langford Exch - Lagoon/Royal Bay", //
						"Langford Exch Via Royal Bay", //
						"Langford Exch Via Lagoon", //
						"Langford Exch", //
						"Bear Mountain - Lagoon/Royal Bay", //
						"Bear Mountain Via Lagoon", //
						"Bear Mountain" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // COLWOOD EXCHANGE - EAST
				if (Arrays.asList( //
						"Langford Exch", //
						"Langford Exch Via Lagoon", //
						"Colwood Exch Via Royal Bay/Lagoon", //
						"Colwood Exch Via Royal Bay", //
						"Colwood Exch Via Lagoon", //
						"Colwood Exch" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 53L) {
			if (gTrip.getDirectionId() == 0) { // COLWOOD EXCHANGE - CLOCKWISE
				if (Arrays.asList( //
						"Colwood Exch Via Atkins - Thetis Lk", //
						"Colwood Exch Via Atkins" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // LANGFORD EXCHANGE - COUNTERCLOCKWISE
				if (Arrays.asList( //
						"Langford Exch Via Atkins - Thetis Lk", //
						"Langford Exch Via Atkins - Theits Lk", //
						"Langford Exch Via Atkins" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.COUNTERCLOCKWISE);
					return;
				}
			}
		} else if (rsn == 54L) {
			if (gTrip.getDirectionId() == 0) { // LANGFORD EXCHANGE - CLOCKWISE
				if (Arrays.asList( //
						"Metchosin" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			}
		} else if (rsn == 55L) {
			if (gTrip.getDirectionId() == 1) { // LANGFORD EXCHANGE - COUNTERCLOCKWISE
				if (Arrays.asList( //
						"Happy Valley To Colwood Exch", //
						"Happy Valley" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.COUNTERCLOCKWISE);
					return;
				}
			}
		} else if (rsn == 56L) {
			if (gTrip.getDirectionId() == 0) { // THETIS HEIGHTS - NORTH
				if (Arrays.asList( //
						"Thetis Heights Via Florence Lake" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // LANGFORD EXCHANGE - SOUTH
				if (Arrays.asList( //
						"Langford Exch" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (rsn == 57L) {
			if (gTrip.getDirectionId() == 0) { // THETIS HEIGHTS - NORTH
				if (Arrays.asList( //
						"Thetis Heights Via Millstream", //
						"Theits Heights Via Millstream" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // LANGFORD EXCHANGE - SOUTH
				if (Arrays.asList( //
						"Langford Exch" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (rsn == 58L) {
			if (gTrip.getDirectionId() == 1) { // GOLDSTREAM MEADOWS - OUTBOUND
				if (Arrays.asList( //
						"Goldstream Mdws" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.OUTBOUND);
					return;
				}
			}
		} else if (rsn == 59L) {
			if (gTrip.getDirectionId() == 1) { // LANGFORD EXCHANGE - COUNTERCLOCKWISE
				if (Arrays.asList( //
						"To Royal Bay Sch Via Triangle Mtn", //
						"Triangle Mtn Via Royal Bay", //
						"Triangle Mtn" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.COUNTERCLOCKWISE);
					return;
				}
			}
		} else if (rsn == 60L) {
			if (gTrip.getDirectionId() == 0) { // LANGFORD EXCHANGE - CLOCKWISE
				if (Arrays.asList( //
						"Langford Exch", //
						"Wishart Via Royal Bay", //
						"Wishart" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			}
		} else if (rsn == 61L) {
			if (gTrip.getDirectionId() == 0) { // SOOKE - WEST
				if (Arrays.asList( //
						"Sooke" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - EAST
				if (Arrays.asList( //
						"Langford - Jacklin/Station", //
						"Langford Exch To 50 Downtown", //
						"Downtown" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 63L) {
			// TODO split?
			if (gTrip.getDirectionId() == 0) { // OTTER POINT - WEST
				if (Arrays.asList( //
						"Otter Point" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			}
		} else if (rsn == 64L) {
			if (gTrip.getDirectionId() == 0) { // SOOKE - CLOCKWISE
				if (Arrays.asList( //
						"To 17 Mile House", //
						"To Langford Exch", //
						"Sooke", //
						"East Sooke To 17 Mile House", //
						"East Sooke To Langford", //
						"East Sooke To Sooke" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			}
			if (gTrip.getDirectionId() == 1) { // EAST SOOKE - ????
				if (Arrays.asList( //
						"East Sooke" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.COUNTERCLOCKWISE);
					return;
				}
			}
		} else if (rsn == 65L) {
			if (gTrip.getDirectionId() == 0) { // SOOKE - WEST
				if (Arrays.asList( //
						"Sooke Via Westhills" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.WEST);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - EAST
				if (Arrays.asList( //
						"Downtown Via Westhills" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.EAST);
					return;
				}
			}
		} else if (rsn == 70L) {
			if (gTrip.getDirectionId() == 0) { // SWARTZ BAY FERRY - NORTH
				if (Arrays.asList( //
						"Swartz Bay Ferry Via Hwy #17" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - SOUTH
				if (Arrays.asList( //
						"To Gorge Only Via Hwy #17", //
						"Downtown Via Hwy #17" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (rsn == 71L) {
			if (gTrip.getDirectionId() == 0) { // SWARTZ BAY FERRY - NORTH
				if (Arrays.asList( //
						"Swartz Bay Ferry Via West Sidney" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - SOUTH
				if (Arrays.asList( //
						"Downtown" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (rsn == 72L) {
			if (gTrip.getDirectionId() == 0) { // SWARTZ BAY FERRY - NORTH
				if (Arrays.asList( //
						"McDonald Park Via Saanichton", //
						"Swartz Bay Ferry Via Saanichton" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - SOUTH
				if (Arrays.asList( //
						"McTavish Exch", //
						"Downtown" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (rsn == 75L) {
			if (gTrip.getDirectionId() == 0) { // SAANICHTON - NORTH
				if (Arrays.asList( //
						"To Keating Only", //
						"Saanichton Exch Via Verdier", //
						"Saanichton Exch" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // DOWNTOWN - SOUTH
				if (Arrays.asList( //
						"Royal Oak Exch To 30 Downtown", //
						"Royal Oak Exch To 31 Downtown", //
						"Downtown" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (rsn == 76L) {
			if (gTrip.getDirectionId() == 0) { // SWARTZ BAY FERRY - NORTH
				if (Arrays.asList( //
						"Swartz Bay Ferry Non-Stop" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // UVIC - SOUTH
				if (Arrays.asList( //
						"UVic - Via Express" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (rsn == 81L) {
			if (gTrip.getDirectionId() == 0) { // SWARTZ BAY FERRY - NORTH
				if (Arrays.asList( //
						"To Sidney Only", //
						"Swartz Bay Ferry" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // BRENTWOOD - SOUTH
				if (Arrays.asList( //
						"Saanichton Exch", //
						"Brentwood To Verdier Only", //
						"Brentwood" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (rsn == 82L) {
			if (gTrip.getDirectionId() == 0) { // SIDNEY - NORTH
				if (Arrays.asList( //
						"Sidney Via Stautw" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // SAANICHTON - SOUTH
				if (Arrays.asList( //
						"To Brentwood Via Stautw", //
						"Saanichton Exch Via Stautw" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (rsn == 83L) {
			if (gTrip.getDirectionId() == 0) { // SIDNEY - NORTH
				if (Arrays.asList( //
						"Sidney Via West Saanich" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // ROYAL OAK - SOUTH
				if (Arrays.asList( //
						"Royal Oak Exch Via West Saanich" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (rsn == 85L) {
			// TODO split
			if (gTrip.getDirectionId() == 0) { // NORTH SAANICH - CLOCKWISE
				if (Arrays.asList( //
						"North Saanich" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // NORTH SAANICH - CLOCKWISE
				if (Arrays.asList( //
						"North Saanich" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.CLOCKWISE);
					return;
				}
			}
		} else if (rsn == 87L) {
			if (gTrip.getDirectionId() == 0) { // SIDNEY - NORTH
				if (Arrays.asList( //
						"Sidney" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // SAANICHTON - SOUTH
				if (Arrays.asList( //
						"Dean Park Via Airport To Saanichton" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		} else if (rsn == 88L) {
			if (gTrip.getDirectionId() == 0) { // SIDNEY - NORTH
				if (Arrays.asList( //
						"Sidney" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.NORTH);
					return;
				}
			} else if (gTrip.getDirectionId() == 1) { // AIRPORT - SOUTH
				if (Arrays.asList( //
						"Airport" //
				).contains(tripHeadsign)) {
					mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), StrategicMappingCommons.SOUTH);
					return;
				}
			}
		}
		throw new MTLog.Fatal("%s: %s: Unexpected trips head-sign '%s' for %s!", mTrip.getRouteId(), rsn, tripHeadsign, gTrip.toStringPlus());
	}

	@Override
	public boolean mergeHeadsign(@NotNull MTrip mTrip, @NotNull MTrip mTripToMerge) {
		List<String> headsignsValues = Arrays.asList(mTrip.getHeadsignValue(), mTripToMerge.getHeadsignValue());
		final long rsn = this.routeIdToShortName.get(mTrip.getRouteId());
		if (rsn == 2L) {
			if (Arrays.asList( //
					JAMES_BAY, // <>
					DOWNTOWN, // <>
					WILLOWS, //
					SOUTH_OAK_BAY //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SOUTH_OAK_BAY, mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 3L) {
			if (Arrays.asList( //
					DOWNTOWN, // <>
					"10 " + R_JUBILEE, //
					JAMES_BAY // ++
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(JAMES_BAY, mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 4L) {
			if (Arrays.asList( //
					GORGE + AND + DOUGLAS, //
					DOWNTOWN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 6L) {
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
		} else if (rsn == 7L) {
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
		} else if (rsn == 8L) {
			if (Arrays.asList( //
					DOUGLAS, //
					RICHMOND + AND + OAK_BAY + " Ave", //
					OAK_BAY //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(OAK_BAY, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					TILLICUM_CENTER, //
					TILLICUM_MALL, //
					INTERURBAN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(INTERURBAN, mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 10L) {
			if (Arrays.asList( //
					VIC_WEST, //
					JAMES_BAY //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(JAMES_BAY, mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 11L) {
			if (Arrays.asList( //
					DOWNTOWN, //
					U_VIC //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(U_VIC, mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 14L) {
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
		} else if (rsn == 15L) {
			if (Arrays.asList( //
					DOWNTOWN, //
					U_VIC //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(U_VIC, mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 21L) {
			if (Arrays.asList( //
					"N " + CAMOSUN, //
					INTERURBAN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(INTERURBAN, mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 22L) {
			if (Arrays.asList( //
					DOWNTOWN, // <>
					"A " + VIC_GENERAL, //
					SPECTRUM_SCHOOL, //
					VIC_GENERAL //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(VIC_GENERAL, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					DOWNTOWN, // <>
					"A " + HILLSIDE_MALL, //
					HILLSIDE_MALL //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(HILLSIDE_MALL, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					DOWNTOWN, // <>
					"A " + HILLSIDE_CENTER, //
					HILLSIDE_CENTER //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(HILLSIDE_CENTER, mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 24L) {
			if (Arrays.asList( //
					DOWNTOWN, //
					ADMIRALS_WALK //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ADMIRALS_WALK, mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 25L) {
			if (Arrays.asList( //
					SHORELINE_SCHOOL, //
					ADMIRALS_WALK //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ADMIRALS_WALK, mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 26L) {
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
		} else if (rsn == 27L) {
			if (Arrays.asList( //
					HILLSIDE, //
					DOWNTOWN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 28L) {
			if (Arrays.asList( //
					MC_KENZIE, //
					HILLSIDE, //
					DOWNTOWN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 30L) {
			if (Arrays.asList( //
					"75 " + SAANICHTON, //
					ROYAL_OAK_EXCH //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ROYAL_OAK_EXCH, mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 31L) {
			if (Arrays.asList( //
					"75 " + SAANICHTON, //
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
		} else if (rsn == 32L) {
			if (Arrays.asList( //
					DOWNTOWN, //
					ROYAL_OAK_EXCH //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ROYAL_OAK_EXCH, mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 39L) {
			if (Arrays.asList( //
					ROYAL_OAK_EXCH, //
					INTERURBAN, //
					WESTHILLS_EXCH //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(WESTHILLS_EXCH, mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 50L) {
			if (Arrays.asList( //
					"61 " + SOOKE, //
					LANGFORD //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(LANGFORD, mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 52L) {
			if (Arrays.asList( //
					LANGFORD_EXCH, // <>
					COLWOOD_EXCH //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(COLWOOD_EXCH, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					LANGFORD_EXCH, // <>
					"Royal Bay Sch", //
					BEAR_MOUTAIN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(BEAR_MOUTAIN, mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 55L) {
			if (Arrays.asList( //
					COLWOOD_EXCH, //
					HAPPY_VLY //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(HAPPY_VLY, mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 59L) {
			if (Arrays.asList( //
					"Royal Bay Sch", //
					"Triangle Mtn" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Triangle Mtn", mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 60L) {
			if (Arrays.asList( //
					"Langford Exch", //
					"Wishart" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Wishart", mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 61L) {
			if (Arrays.asList( //
					LANGFORD, //
					"50 " + DOWNTOWN, //
					DOWNTOWN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 64L) {
			if (Arrays.asList( //
					"17 " + MILE_HOUSE, //
					"Langford Exch", //
					LANGFORD, //
					SOOKE //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SOOKE, mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 70L) {
			if (Arrays.asList( //
					GORGE, //
					DOWNTOWN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 72L) {
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
		} else if (rsn == 75L) {
			if (Arrays.asList( //
					KEATING, //
					SAANICHTON_EXCH //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SAANICHTON_EXCH, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					ROYAL_OAK_EXCH, //
					"30 " + DOWNTOWN, //
					"31 " + DOWNTOWN, //
					DOWNTOWN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (rsn == 81L) {
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
		} else if (rsn == 82L) {
			if (Arrays.asList( //
					SAANICHTON_EXCH, //
					BRENTWOOD //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(BRENTWOOD, mTrip.getHeadsignId());
				return true;
			}
		}
		throw new MTLog.Fatal("%s: Unexpected trips to merges %s & %s!", rsn, mTrip, mTripToMerge);
	}

	private static final Pattern EXCHANGE = Pattern.compile("((^|\\W)(exchange)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String EXCHANGE_REPLACEMENT = "$2" + EXCH + "$4";

	private static final Pattern HEIGHTS = Pattern.compile("((^|\\W)(Hghts)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String HEIGHTS_REPLACEMENT = "$2Hts$4";

	private static final Pattern ENDS_WITH_EXPRESS = Pattern.compile("( express.*$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern ENDS_WITH_DASH = Pattern.compile("( - .*$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern ENDS_WITH_NON_STOP = Pattern.compile("( non-stop$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern ENDS_WITH_ONLY = Pattern.compile("( only$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_RSN = Pattern.compile("(^\\d+)", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, tripHeadsign, getIgnoredUpperCaseWords());
		tripHeadsign = STARTS_WITH_RSN.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = EXCHANGE.matcher(tripHeadsign).replaceAll(EXCHANGE_REPLACEMENT);
		tripHeadsign = HEIGHTS.matcher(tripHeadsign).replaceAll(HEIGHTS_REPLACEMENT);
		tripHeadsign = ENDS_WITH_DASH.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		tripHeadsign = ENDS_WITH_EXPRESS.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = ENDS_WITH_NON_STOP.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = ENDS_WITH_ONLY.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = CleanUtils.cleanSlashes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@NotNull
	private String[] getIgnoredUpperCaseWords() {
		return new String[]{"BC", "HCP", "HMC", "SEAPARC", "VI"};
	}

	private static final Pattern UVIC = Pattern.compile("((^|\\W)(uvic)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String UVIC_REPLACEMENT = "$2" + U_VIC + "$4";

	private static final Pattern STARTS_WITH_DCOM = Pattern.compile("(^(\\(-DCOM-\\)))", Pattern.CASE_INSENSITIVE);
	private static final Pattern STARTS_WITH_IMPL = Pattern.compile("(^(\\(-IMPL-\\)))", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, gStopName, getIgnoredUpperCaseWords());
		gStopName = STARTS_WITH_DCOM.matcher(gStopName).replaceAll(EMPTY);
		gStopName = STARTS_WITH_IMPL.matcher(gStopName).replaceAll(EMPTY);
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.CLEAN_AND.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		gStopName = EXCHANGE.matcher(gStopName).replaceAll(EXCHANGE_REPLACEMENT);
		gStopName = UVIC.matcher(gStopName).replaceAll(UVIC_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@Override
	public int getStopId(@NotNull GStop gStop) { // used by GTFS-RT
		return super.getStopId(gStop);
	}
}
