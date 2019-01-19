package com.syv.takecare.takecare.fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.syv.takecare.takecare.activities.TakeCareActivity;
import com.syv.takecare.takecare.adapters.SectionsPageAdapter;
import com.syv.takecare.takecare.customViews.CustomInfoWindow;
import com.syv.takecare.takecare.activities.ItemInfoActivity;
import com.syv.takecare.takecare.R;
import com.syv.takecare.takecare.activities.TakerMenuActivity;
import com.syv.takecare.takecare.customViews.CustomViewPager;

import java.util.HashMap;

public class FeedMapFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "FeedMapFragment";
    private static final String EXTRA_ITEM_ID = "Item Id";
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private GoogleMap mMap;
    private HashMap<String, Marker> markers;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private final LatLng mDefaultLocation = new LatLng(32.777751, 35.021508);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed_map, container, false);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        markers = new HashMap<String, Marker>();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.feed_map);
        mapFragment.getMapAsync(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        return view;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(0, 0, 40, 200);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
        mMap.setOnMyLocationClickListener(onMyLocationClickListener);

        updateLocationUI();
        getDeviceLocation();

        mMap.setMinZoomPreference(11);
        Query query = db.collection("items")
                .whereEqualTo("displayStatus", true);

        String queryCategoriesFilter = ((TakerMenuActivity) getActivity()).getQueryCategoriesFilter();
        String queryPickupMethodFilter = ((TakerMenuActivity) getActivity()).getQueryPickupMethodFilter();
        if (queryCategoriesFilter != null && queryPickupMethodFilter != null) {
            // Filter by categories and pickup method
            Log.d(TAG, "setUpAdapter: query has: category: " + queryCategoriesFilter + " pickup: " + queryPickupMethodFilter);
            query = db.collection("items")
                    .whereEqualTo("category", queryCategoriesFilter)
                    .whereEqualTo("pickupMethod", queryPickupMethodFilter)
                    .whereEqualTo("displayStatus", true);
        } else if (queryCategoriesFilter != null) {
            // Filter by categories
            Log.d(TAG, "setUpAdapter: query has: category: " + queryCategoriesFilter);
            query = db.collection("items")
                    .whereEqualTo("category", queryCategoriesFilter)
                    .whereEqualTo("displayStatus", true);
        } else if (queryPickupMethodFilter != null) {
            // Filter by pickup method
            Log.d(TAG, "setUpAdapter: query has: pickup: " + queryPickupMethodFilter);
            query = db.collection("items")
                    .whereEqualTo("pickupMethod", queryPickupMethodFilter)
                    .whereEqualTo("displayStatus", true);
        }
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d(TAG, "error loading documents: " + e.getMessage());
                    return;
                }
                if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                    Log.d(TAG, "did not find any documents");
                    return;
                }

                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    DocumentSnapshot doc = dc.getDocument();
                    String itemId = doc.getReference().getId();

                    if (dc.getType() == DocumentChange.Type.REMOVED) {
                        Marker markerToDelete = markers.get(itemId);
                        try {
                            markerToDelete.remove();
                        } catch (NullPointerException nullptrExc) {
                            Log.d(TAG, "onEvent: tried to remove a non-existing marker");
                        }
                        markers.remove(itemId);
                        continue;
                    }

                    if (dc.getType() == DocumentChange.Type.MODIFIED) {
                        if (doc.get("displayStatus") == null || !(boolean) doc.get("displayStatus")) {
                            Marker markerToDelete = markers.get(itemId);
                            try {
                                markerToDelete.remove();
                            } catch (NullPointerException nullptrExc) {
                                Log.d(TAG, "onEvent: tried to remove a non-existing marker");
                            }
                            markers.remove(itemId);
                            continue;
                        }
                    }

                    GeoPoint itemLocation = (GeoPoint) doc.get("location");
                    if (itemLocation != null) {
                        MarkerOptions testMarker = new MarkerOptions();
                        String category = (String) doc.get("category");
                        int iconId;
                        switch (category) {
                            case "Food":
                                iconId = R.drawable.pizza_markernobg;
                                break;
                            case "Study Material":
                                iconId = R.drawable.book_marker;
                                break;
                            case "Households":
                                iconId = R.drawable.households_marker;
                                break;
                            case "Lost & Found":
                                iconId = R.drawable.lost_and_found_marker;
                                break;
                            case "Hitchhikes":
                                iconId = R.drawable.car_marker;
                                break;
                            default:
                                iconId = R.drawable.treasure_marker;
                        }
                        String title = (String) doc.get("title");
                        String pickupMethod = (String) doc.get("pickupMethod");
                        try {
                            BitmapDescriptor resultImage = BitmapDescriptorFactory.fromBitmap(resizeMapIcons(iconId, 170, 170));
                            testMarker.position(new LatLng(itemLocation.getLatitude(), itemLocation.getLongitude()))
                                    .icon(resultImage)
                                    .title(title)
                                    .snippet(pickupMethod);
                        } catch (Exception exc) {
                            return;
                        }
                        CustomInfoWindow customInfoWindow = new CustomInfoWindow(getContext());
                        mMap.setInfoWindowAdapter(customInfoWindow);
                        Marker m = mMap.addMarker(testMarker);
                        m.setTag(doc);
                        markers.put(itemId, m);

                    }
                }
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                DocumentSnapshot doc = (DocumentSnapshot) marker.getTag();
                String itemId = doc.getReference().getId();
                String publisher = (String) doc.get("publisher");
                Intent intent = new Intent(getActivity().getApplicationContext(), ItemInfoActivity.class);
                intent.putExtra(EXTRA_ITEM_ID, itemId);
                intent.putExtra(Intent.EXTRA_UID, publisher);
                startActivity(intent);
            }
        });

    }

    public Bitmap resizeMapIcons(int markerIcon, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), markerIcon);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }


    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.getResult();
                        if (mLastKnownLocation == null) {
                            return;
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()), 15));
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());
                        mMap.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(mDefaultLocation, 15));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private GoogleMap.OnMyLocationClickListener onMyLocationClickListener =
            new GoogleMap.OnMyLocationClickListener() {
                @Override
                public void onMyLocationClick(@NonNull Location location) {

                    mMap.setMinZoomPreference(12);

                    CircleOptions circleOptions = new CircleOptions();
                    circleOptions.center(new LatLng(location.getLatitude(),
                            location.getLongitude()));

                    circleOptions.radius(200);
                    circleOptions.fillColor(Color.RED);
                    circleOptions.strokeWidth(6);

                    mMap.addCircle(circleOptions);
                }
            };

    private GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener =
            new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    mMap.setMinZoomPreference(15);
                    return false;
                }
            };
}
