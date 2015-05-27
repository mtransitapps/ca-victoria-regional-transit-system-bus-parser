package org.mtransit.parser.ca_victoria_regional_transit_system_bus;

import java.util.HashSet;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MSpec;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

// http://bctransit.com/*/footer/open-data
// http://bctransit.com/servlet/bctransit/data/GTFS.zip
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
		System.out.printf("Generating Victoria Regional Transit System bus data...\n");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("Generating Victoria Regional Transit System bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	private static final String INCLUDE_AGENCY_ID = "1"; // Victoria Regional Transit System only

	@Override
	public boolean excludeRoute(GRoute gRoute) {
		if (!INCLUDE_AGENCY_ID.equals(gRoute.agency_id)) {
			return true;
		}
		return super.excludeRoute(gRoute);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
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
		return Long.parseLong(gRoute.route_short_name); // use route short name as route ID
	}

	@Override
	public String getRouteLongName(GRoute gRoute) {
		String routeLongName = gRoute.route_long_name;
		routeLongName = MSpec.cleanNumbers(routeLongName);
		routeLongName = MSpec.cleanStreetTypes(routeLongName);
		return MSpec.cleanLabel(routeLongName);
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
		if (StringUtils.isEmpty(gRoute.route_color)) {
			return AGENCY_COLOR_BLUE;
		}
		return super.getRouteColor(gRoute);
	}

	private static final String DOWNTOWN = "Downtown";
	private static final String OAK_BAY = "Oak Bay";
	private static final String BEACON_HILL = "Beacon Hl";
	private static final String TILLICUM_MALL = "Tillicum Mall";
	private static final String ROYAL_OAK = "Royal Oak";
	private static final String ROYAL_OAK_EX = ROYAL_OAK + " Ex";
	private static final String CAMOSUN = "Camosun";
	private static final String ROYAL_OAK_CAMOSUN_ROYAL_ROADS = ROYAL_OAK + " / " + CAMOSUN + " / Royal Roads";
	private static final String JAMES_BAY = "James Bay";
	private static final String MAJESTIC = "Majestic";
	private static final String DOCKYARD = "Dockyard";
	private static final String MAPLEWOOD = "Maplewood";
	private static final String ADMIRALS_WALK = "Admirals Walk";
	private static final String VIC_GENERAL = "Vic General";
	private static final String HILLSIDE_MALL = "Hillside Mall";
	private static final String U_VIC = "UVic";
	private static final String SONGHEES = "Songhees";
	private static final String BRENTWOOD = "Brentwood";
	private static final String ROYAL_OAK_DOWNTOWN = ROYAL_OAK + " / " + DOWNTOWN;
	private static final String SAANICHTON = "Saanichton";
	private static final String SWARTZ_BAY = "Swartz Bay";
	private static final String E_SOOKE_SOOKE = "E Sooke / Sooke";
	private static final String LANGFORD = "Langford";
	private static final String LANGFORD_DOWNTOWN = LANGFORD + " / " + DOWNTOWN;
	private static final String THETIS_HTS = "Thetis Hts";
	private static final String MILLSTREAM_BEAR_MTN = "Millstream / Bear Mtn";
	private static final String COLWOOD_EX = "Colwood Ex";

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (mRoute.id == 2l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(OAK_BAY, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 3l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(BEACON_HILL, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 4l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 7l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 8l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(OAK_BAY, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(TILLICUM_MALL, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 10l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(SONGHEES, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 14l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(U_VIC, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 21l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CAMOSUN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 22l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(VIC_GENERAL, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(HILLSIDE_MALL, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 24l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ADMIRALS_WALK, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 25l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(MAPLEWOOD, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ADMIRALS_WALK, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 26l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(U_VIC, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOCKYARD, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 27l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 28l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(MAJESTIC, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 30l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(ROYAL_OAK_EX, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(JAMES_BAY, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 31l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(ROYAL_OAK_EX, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(JAMES_BAY, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 32l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ROYAL_OAK_EX, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 39l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(U_VIC, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ROYAL_OAK_CAMOSUN_ROYAL_ROADS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 50l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(LANGFORD, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 52l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(COLWOOD_EX, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(MILLSTREAM_BEAR_MTN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 57l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(THETIS_HTS, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 61l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(LANGFORD_DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 64l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(E_SOOKE_SOOKE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 72l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SWARTZ_BAY, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 75l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SAANICHTON, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ROYAL_OAK_DOWNTOWN, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 81l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(SWARTZ_BAY, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(BRENTWOOD, gTrip.direction_id);
				return;
			}
		}
		mTrip.setHeadsignString(cleanTripHeadsign(gTrip.trip_headsign), gTrip.direction_id);
	}

	private static final Pattern EXCHANGE = Pattern.compile("(exchange)", Pattern.CASE_INSENSITIVE);
	private static final String EXCHANGE_REPLACEMENT = "Ex";

	private static final Pattern STARTS_WITH_NUMBER = Pattern.compile("(^[\\d]+[\\S]*)", Pattern.CASE_INSENSITIVE);

	private static final Pattern ENDS_WITH_EXPRESS = Pattern.compile("( express.*$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern ENDS_WITH_VIA = Pattern.compile("( via .*$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_TO = Pattern.compile("(^.* to )", Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = EXCHANGE.matcher(tripHeadsign).replaceAll(EXCHANGE_REPLACEMENT);
		tripHeadsign = ENDS_WITH_VIA.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = STARTS_WITH_TO.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = ENDS_WITH_EXPRESS.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = STARTS_WITH_NUMBER.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = MSpec.cleanStreetTypes(tripHeadsign);
		return MSpec.cleanLabel(tripHeadsign);
	}

	private static final Pattern STARTS_WITH_BOUND = Pattern.compile("(^(east|west|north|south)bound)", Pattern.CASE_INSENSITIVE);

	private static final Pattern AT = Pattern.compile("( at )", Pattern.CASE_INSENSITIVE);
	private static final String AT_REPLACEMENT = " / ";

	private static final Pattern UVIC = Pattern.compile("((^|\\W){1}(uvic)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String UVIC_REPLACEMENT = "$2" + U_VIC + "$4";

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = STARTS_WITH_BOUND.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		gStopName = AT.matcher(gStopName).replaceAll(AT_REPLACEMENT);
		gStopName = EXCHANGE.matcher(gStopName).replaceAll(EXCHANGE_REPLACEMENT);
		gStopName = UVIC.matcher(gStopName).replaceAll(UVIC_REPLACEMENT);
		gStopName = MSpec.cleanStreetTypes(gStopName);
		return MSpec.cleanLabel(gStopName);
	}
}
