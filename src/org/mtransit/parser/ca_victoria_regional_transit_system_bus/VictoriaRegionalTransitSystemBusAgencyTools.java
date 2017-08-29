package org.mtransit.parser.ca_victoria_regional_transit_system_bus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.mt.data.MTrip;

// https://bctransit.com/*/footer/open-data
// https://bctransit.com/servlet/bctransit/data/GTFS - Victoria
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
		System.out.printf("\nGenerating Victoria Regional Transit System bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this, true);
		super.start(args);
		System.out.printf("\nGenerating Victoria Regional Transit System bus data... DONE in %s.\n",
				Utils.getPrettyDuration(System.currentTimeMillis() - start));
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

	private static final String SLASH = " / ";
	private static final String AND = " & ";
	private static final String EXCH = "Exch";
	private static final String ONLY = "Only";
	private static final String _ONLY = " " + ONLY;

	private static final String DOWNTOWN = "Downtown";
	private static final String OAK_BAY = "Oak Bay";
	private static final String BEACON_HILL = "Beacon Hl";
	private static final String ROYAL_OAK = "Royal Oak";
	private static final String ROYAL_OAK_EXCH = ROYAL_OAK + " " + EXCH;
	private static final String CAMOSUN = "Camosun";
	private static final String ROYAL_ROADS = "Royal Roads";
	private static final String JAMES_BAY = "James Bay";
	private static final String MAJESTIC = "Majestic";
	private static final String DOCKYARD = "Dockyard";
	private static final String ADMIRALS_WALK = "Admirals Walk";
	private static final String HILLSIDE = "Hillside";
	private static final String HILLSIDE_MALL = HILLSIDE + " Mall";
	private static final String U_VIC = "UVic";
	private static final String SONGHEES = "Songhees";
	private static final String BRENTWOOD = "Brentwood";
	private static final String SAANICHTON = "Saanichton";
	private static final String SAANICHTON_EXCH = SAANICHTON + " " + EXCH;
	private static final String SWARTZ_BAY = "Swartz Bay";
	private static final String SWARTZ_BAY_FERRY = SWARTZ_BAY + " Ferry";
	private static final String SOOKE = "Sooke";
	private static final String EAST_SOOKE = "East " + SOOKE;
	private static final String LANGFORD = "Langford";
	private static final String LANGFORD_EXCH = LANGFORD + " " + EXCH;
	private static final String THETIS_HTS = "Thetis Hts";
	private static final String COLWOOD_EXCH = "Colwood " + EXCH;
	private static final String HAPPY_VLY = "Happy Vly";
	private static final String TILLICUM_MALL = "Tillicum Mall";
	private static final String SPECTRUM_SCHOOL = "Spectrum School";
	private static final String GORGE = "Gorge";
	private static final String INTERURBAN = "Interurban";
	private static final String MILE_HOUSE = "Mile House";
	private static final String VERDIER = "Verdier";
	private static final String OLDFIELD = "Oldfield";
	private static final String SIDNEY = "Sidney";
	private static final String WESTERN_SPEEDWAY = "Western Speedway";
	private static final String BEAR_MOUTAIN = "Bear Mtn";
	private static final String MC_DONALD_PARK = "McDonald Pk";
	private static final String MC_TAVISH = "McTavish";
	private static final String VIC_GENERAL = "Vic General";
	private static final String UPTOWN = "Uptown";
	private static final String RICHMOND = "Richmond";
	private static final String MC_KENZIE = "McKenzie";
	private static final String DOUGLAS = "Douglas";
	private static final String MENZIES = "Menzies";
	private static final String WILLOWS = "Willows";

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), gTrip.getDirectionId());
	}

	@Override
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		List<String> headsignsValues = Arrays.asList(mTrip.getHeadsignValue(), mTripToMerge.getHeadsignValue());
		if (mTrip.getRouteId() == 2l) {
			if (Arrays.asList( //
					"A " + WILLOWS, //
					OAK_BAY //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(OAK_BAY, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					DOWNTOWN, //
					U_VIC, //
					"N " + U_VIC //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 3l) {
			if (Arrays.asList( //
					BEACON_HILL, //
					BEACON_HILL + SLASH + MENZIES, //
					MENZIES //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(BEACON_HILL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 4l) {
			if (Arrays.asList( //
					DOWNTOWN, //
					HILLSIDE + AND + DOUGLAS + _ONLY //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 6l) {
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
		} else if (mTrip.getRouteId() == 7l) {
			if (Arrays.asList( //
					"A " + WILLOWS, //
					"N " + DOWNTOWN, //
					"N " + DOWNTOWN + _ONLY, //
					OAK_BAY //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(OAK_BAY, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"A " + U_VIC, //
					"N " + U_VIC, //
					U_VIC //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(U_VIC, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 8l) {
			if (Arrays.asList( //
					"Douglas - Mayfair Mall", //
					OAK_BAY, //
					OAK_BAY + AND + RICHMOND //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(OAK_BAY, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					INTERURBAN, //
					TILLICUM_MALL //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(INTERURBAN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 10l) {
			if (Arrays.asList( //
					DOUGLAS + _ONLY, //
					SONGHEES //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SONGHEES, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 11l) {
			if (Arrays.asList( //
					DOWNTOWN, //
					U_VIC //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(U_VIC, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 14l) {
			if (Arrays.asList( //
					DOWNTOWN, // SAME
					U_VIC //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(U_VIC, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					DOWNTOWN, // SAME
					VIC_GENERAL //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(VIC_GENERAL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 15l) {
			if (Arrays.asList( //
					DOWNTOWN, //
					U_VIC //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(U_VIC, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 21l) {
			if (Arrays.asList( //
					CAMOSUN, //
					CAMOSUN + "-VITP", //
					CAMOSUN + "-Viaduct" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CAMOSUN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 22l) {
			if (Arrays.asList( //
					"A " + VIC_GENERAL + " S. Vale-Watkiss", //
					"N " + CAMOSUN + "-" + INTERURBAN, //
					VIC_GENERAL, //
					VIC_GENERAL + " - Watkiss Way", //
					SPECTRUM_SCHOOL).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(VIC_GENERAL, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"A " + HILLSIDE_MALL, //
					DOWNTOWN, //
					HILLSIDE_MALL, //
					"N " + DOWNTOWN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(HILLSIDE_MALL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 24l) {
			if (Arrays.asList( //
					ADMIRALS_WALK, //
					DOWNTOWN //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ADMIRALS_WALK, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 25l) {
			if (Arrays.asList( //
					ADMIRALS_WALK, //
					"Shoreline Sch", //
					COLWOOD_EXCH //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(COLWOOD_EXCH, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 26l) {
			if (Arrays.asList( //
					UPTOWN + _ONLY, //
					U_VIC //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(U_VIC, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					DOCKYARD, //
					UPTOWN + _ONLY //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOCKYARD, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 27l) {
			if (Arrays.asList( //
					BEACON_HILL, //
					DOWNTOWN, //
					HILLSIDE + _ONLY, //
					"X " + DOWNTOWN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 28l) {
			if (Arrays.asList( //
					MAJESTIC, //
					"X " + MAJESTIC //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(MAJESTIC, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					BEACON_HILL, //
					DOWNTOWN, //
					HILLSIDE + _ONLY, //
					MC_KENZIE + _ONLY //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 30l) {
			if (Arrays.asList( //
					ROYAL_OAK_EXCH, //
					SAANICHTON //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ROYAL_OAK_EXCH, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					BEACON_HILL, //
					JAMES_BAY, //
					DOWNTOWN //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(JAMES_BAY, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 31l) {
			if (Arrays.asList( //
					DOWNTOWN, //
					ROYAL_OAK_EXCH, //
					SAANICHTON //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ROYAL_OAK_EXCH, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					GORGE + _ONLY, //
					JAMES_BAY //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(JAMES_BAY, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 32l) {
			if (Arrays.asList( //
					DOWNTOWN, //
					ROYAL_OAK_EXCH //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ROYAL_OAK_EXCH, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 39l) {
			if (Arrays.asList( //
					ROYAL_OAK_EXCH, //
					U_VIC //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(U_VIC, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					CAMOSUN + "-" + INTERURBAN, //
					COLWOOD_EXCH, //
					"Westhills Exch", //
					ROYAL_OAK_EXCH, //
					ROYAL_ROADS //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ROYAL_ROADS, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 50l) {
			if (Arrays.asList( //
					LANGFORD, //
					SOOKE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(LANGFORD, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 52l) {
			if (Arrays.asList( //
					COLWOOD_EXCH, //
					LANGFORD_EXCH + _ONLY //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(COLWOOD_EXCH, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					BEAR_MOUTAIN, //
					LANGFORD_EXCH, //
					LANGFORD_EXCH + _ONLY, //
					WESTERN_SPEEDWAY //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(BEAR_MOUTAIN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 55l) {
			if (Arrays.asList( //
					COLWOOD_EXCH, //
					HAPPY_VLY //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(HAPPY_VLY, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 57l) {
			if (Arrays.asList( //
					LANGFORD_EXCH + _ONLY, //
					THETIS_HTS //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(THETIS_HTS, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 61l) {
			if (Arrays.asList( //
					DOWNTOWN, //
					LANGFORD + "/" + DOWNTOWN, //
					LANGFORD, //
					LANGFORD_EXCH, //
					"X " + DOWNTOWN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					SOOKE, //
					"X " + SOOKE //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SOOKE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 64l) {
			if (Arrays.asList( //
					EAST_SOOKE, //
					LANGFORD, //
					MILE_HOUSE, //
					SOOKE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(EAST_SOOKE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 72l) {
			if (Arrays.asList( //
					MC_DONALD_PARK, //
					SWARTZ_BAY_FERRY //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SWARTZ_BAY_FERRY, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					DOWNTOWN, //
					MC_TAVISH + _ONLY //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 75l) {
			if (Arrays.asList( //
					OLDFIELD, //
					"Keating" + _ONLY, //
					SAANICHTON_EXCH //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SAANICHTON_EXCH, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					DOWNTOWN, //
					ROYAL_OAK_EXCH //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 81l) {
			if (Arrays.asList( //
					BRENTWOOD, //
					"Brentwood Butchart Grdns", //
					"Keating", //
					OLDFIELD, //
					SAANICHTON_EXCH, //
					VERDIER + _ONLY //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(BRENTWOOD, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					SWARTZ_BAY_FERRY, //
					SIDNEY + _ONLY //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SWARTZ_BAY_FERRY, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 83l) {
			if (Arrays.asList( //
					BRENTWOOD, //
					ROYAL_OAK_EXCH //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ROYAL_OAK_EXCH, mTrip.getHeadsignId());
				return true;
			}
		}
		if (isGoodEnoughAccepted()) {
			return super.mergeHeadsign(mTrip, mTripToMerge);
		}
		System.out.printf("\nUnexpected trips to merges %s & %s!s\n", mTrip, mTripToMerge);
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

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = EXCHANGE.matcher(tripHeadsign).replaceAll(EXCHANGE_REPLACEMENT);
		tripHeadsign = HEIGHTS.matcher(tripHeadsign).replaceAll(HEIGHTS_REPLACEMENT);
		tripHeadsign = ENDS_WITH_VIA.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = STARTS_WITH_TO.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = ENDS_WITH_EXPRESS.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = STARTS_WITH_NUMBER.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern STARTS_WITH_BOUND = Pattern.compile("(^(east|west|north|south)bound)", Pattern.CASE_INSENSITIVE);

	private static final Pattern UVIC = Pattern.compile("((^|\\W){1}(uvic)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String UVIC_REPLACEMENT = "$2" + U_VIC + "$4";

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = STARTS_WITH_BOUND.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.CLEAN_AND.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		gStopName = EXCHANGE.matcher(gStopName).replaceAll(EXCHANGE_REPLACEMENT);
		gStopName = UVIC.matcher(gStopName).replaceAll(UVIC_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}
