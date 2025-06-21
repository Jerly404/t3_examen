package com.hcondor.t3_examen.ui.maps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.hcondor.t3_examen.R
import com.hcondor.t3_examen.data.local.*
import com.hcondor.t3_examen.databinding.ActivityMapsBinding
import com.hcondor.t3_examen.ui.area.AreaUtils
import com.hcondor.t3_examen.ui.area.LatLng
import com.hcondor.t3_examen.ui.distance.DistanceUtils
import com.hcondor.t3_examen.ui.history.HistoryActivity
import com.hcondor.t3_examen.ui.markers.AddMarkerDialog
import com.mapbox.geojson.*
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.FillLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.plugin.gestures.gestures
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MapsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private val markerList = mutableListOf<PlaceEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val accessToken = getString(R.string.mapbox_access_token)
        val resourceOptions = ResourceOptions.Builder().accessToken(accessToken).build()
        val mapInitOptions = MapInitOptions(this, resourceOptions)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestLocationPermissions()

        mapView = binding.mapView
        mapboxMap = mapView.getMapboxMap()

        mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(Point.fromLngLat(-77.0428, -12.0464))
                .zoom(12.0)
                .build()
        )

        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
            initAnnotations()
            loadMarkers()
            mapView.gestures.apply {
                pinchToZoomEnabled = true
                scrollEnabled = true
                rotateEnabled = true
            }
            mapboxMap.addOnMapLongClickListener { point ->
                val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                showAddMarkerDialog(point.latitude(), point.longitude(), fechaHoy)
                true
            }
        }

        binding.btnDistancia.setOnClickListener {
            if (markerList.size < 2) {
                Toast.makeText(this, getString(R.string.error_min_distance), Toast.LENGTH_SHORT).show()
            } else {
                val totalDistance = calculateTotalDistance(markerList)
                Toast.makeText(this, getString(R.string.result_distance, totalDistance / 1000), Toast.LENGTH_LONG).show()
            }
        }

        binding.btnArea.setOnClickListener {
            if (markerList.size < 3) {
                Toast.makeText(this, getString(R.string.error_min_area), Toast.LENGTH_SHORT).show()
            } else {
                val area = calculateArea(markerList)
                Toast.makeText(this, getString(R.string.result_area, area), Toast.LENGTH_LONG).show()
            }
        }

        binding.btnClear.setOnClickListener {
            if (markerList.isNotEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle("Confirmar")
                    .setMessage("¿Deseas mover los marcadores al historial y limpiar el mapa?")
                    .setPositiveButton("Sí") { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            val db = AppDatabase.getDatabase(applicationContext)
                            val historyDao = db.placeHistoryDao()
                            val placeDao = db.placeDao()
                            markerList.forEach {
                                val h = PlaceHistoryEntity(
                                    name = it.name,
                                    date = it.date,
                                    note = it.note,
                                    latitude = it.latitude,
                                    longitude = it.longitude
                                )
                                historyDao.insert(h)
                            }
                            placeDao.deleteAll()
                            runOnUiThread {
                                pointAnnotationManager.deleteAll()
                                markerList.clear()
                                removeDrawnLines()
                                Toast.makeText(this@MapsActivity, "Marcadores movidos al historial.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

        binding.btnHistorial.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1001
            )
        }
    }

    private fun initAnnotations() {
        val annotationApi = mapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager()
    }

    private fun showAddMarkerDialog(lat: Double, lon: Double, fechaHoy: String) {
        AddMarkerDialog(fechaHoy) { name, date, note ->
            val place = PlaceEntity(
                name = name,
                date = date,
                note = note,
                latitude = lat,
                longitude = lon
            )
            CoroutineScope(Dispatchers.IO).launch {
                AppDatabase.getDatabase(applicationContext).placeDao().insert(place)
                runOnUiThread {
                    addMarkerToMap(place)
                    markerList.add(place)
                    drawRedLineBetweenMarkers()
                }
            }
        }.show(supportFragmentManager, "AddMarkerDialog")
    }

    private fun loadMarkers() {
        CoroutineScope(Dispatchers.IO).launch {
            val places = AppDatabase.getDatabase(applicationContext).placeDao().getAllNow()
            runOnUiThread {
                places.forEach {
                    addMarkerToMap(it)
                    markerList.add(it)
                }
                drawRedLineBetweenMarkers()
            }
        }
    }

    private fun addMarkerToMap(place: PlaceEntity) {
        val point = Point.fromLngLat(place.longitude, place.latitude)
        val icon = BitmapFactory.decodeResource(resources, R.drawable.marker_cyberpunk)

        val markerOptions = PointAnnotationOptions()
            .withPoint(point)
            .withIconImage(icon)
            .withIconSize(0.030) // 🔸 Tamaño visual aprox. 32px si la imagen es de 128px
            .withTextField(place.name)
            .withTextColor("#39FF14")
            .withTextSize(14.0)

        pointAnnotationManager.create(markerOptions)
        drawRedLineBetweenMarkers()
    }

    private fun drawRedLineBetweenMarkers() {
        removeDrawnLines()
        if (markerList.size >= 2) {
            val points = markerList.map { Point.fromLngLat(it.longitude, it.latitude) }
            val style = mapboxMap.getStyle() ?: return

            if (markerList.size == 2) {
                val lineSource = GeoJsonSource.Builder("line-source")
                    .geometry(LineString.fromLngLats(points))
                    .build()
                style.addSource(lineSource)

                val lineLayer = LineLayer("line-layer", "line-source")
                    .lineColor("#FF0000")
                    .lineWidth(4.0)
                    .lineCap(LineCap.ROUND)
                    .lineJoin(LineJoin.ROUND)
                style.addLayer(lineLayer)

            } else {
                val polygonPoints = mutableListOf<List<Point>>()
                polygonPoints.add(points + points.first())
                val polygonSource = GeoJsonSource.Builder("polygon-source")
                    .geometry(Polygon.fromLngLats(polygonPoints))
                    .build()
                style.addSource(polygonSource)

                val fillLayer = FillLayer("polygon-layer", "polygon-source")
                    .fillColor(Expression.rgba(255.0, 0.0, 0.0, 0.5)) // rojo con 50% opacidad
                style.addLayer(fillLayer)
            }
        }
    }

    private fun removeDrawnLines() {
        val style = mapboxMap.getStyle() ?: return
        listOf("line-layer", "line-source", "polygon-layer", "polygon-source").forEach {
            if (style.styleLayerExists(it)) style.removeStyleLayer(it)
            if (style.styleSourceExists(it)) style.removeStyleSource(it)
        }
    }

    private fun calculateTotalDistance(places: List<PlaceEntity>): Double {
        var total = 0.0
        for (i in 0 until places.size - 1) {
            val p1 = places[i]
            val p2 = places[i + 1]
            total += DistanceUtils.haversine(
                p1.latitude, p1.longitude,
                p2.latitude, p2.longitude
            )
        }
        return total
    }

    private fun calculateArea(places: List<PlaceEntity>): Double {
        val latLngList = places.map {
            LatLng(it.latitude, it.longitude)
        }
        return AreaUtils.calculateArea(latLngList)
    }
}
