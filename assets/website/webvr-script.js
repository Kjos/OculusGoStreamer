var camera, scene, renderer, mesh, material;
var drawStartPos = new THREE.Vector2();

var screenSize = 300;

function webvrInit() {
	if (!Detector.webgl) Detector.addGetWebGLMessage();

	console.log("WebVR init");

	camera = new THREE.PerspectiveCamera( 90, window.innerWidth / window.innerHeight, 1, 2000 );

	scene = new THREE.Scene();

	material = new THREE.MeshBasicMaterial();
	material.map = new THREE.Texture( canvas );

	mesh = new THREE.Mesh( new THREE.PlaneGeometry( screenSize, screenSize ), material );
	mesh.position.set(0, 0, -500);
	scene.add( mesh );

	renderer = new THREE.WebGLRenderer( { antialias: true } );
	renderer.setPixelRatio( window.devicePixelRatio );
	renderer.setSize(window.innerWidth, window.innerHeight);
	renderer.vr.enabled = true;

	document.body.appendChild( renderer.domElement );
	document.body.appendChild( WEBVR.createButton( renderer ) );

	renderer.animate( webvrAnimate );

	window.onresize = webvrWindowResize;
}

function webvrUpdate() {
	material.map.needsUpdate = true;
}

function webvrUpdateAspect(aspect) {
	if (!scene) return;

	while(scene.children.length > 0){ 
	    scene.remove(scene.children[0]); 
	}

	mesh = new THREE.Mesh( new THREE.PlaneGeometry( screenSize * aspect, screenSize ), material );
	mesh.position.set(0, 0, -500);
	scene.add( mesh );
}

function webvrWindowResize() {
	sendCommand("window", ['1024', '512']);

	camera.aspect = window.innerWidth / window.innerHeight;
	camera.updateProjectionMatrix();

	renderer.setSize( window.innerWidth, window.innerHeight );
}

function webvrAnimate() {
	renderer.render( scene, camera );
}
