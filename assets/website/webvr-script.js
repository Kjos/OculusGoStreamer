var camera, scene, renderer, mesh, material;
var drawStartPos = new THREE.Vector2();

var screenSize = 700;

function webvrInit() {
	if (!Detector.webgl) Detector.addGetWebGLMessage();

	console.log("WebVR init");

	camera = new THREE.PerspectiveCamera( 90, window.innerWidth / window.innerHeight, 1, 2000 );
	camera.position.z = 500;

	scene = new THREE.Scene();

	material = new THREE.MeshBasicMaterial();
	material.map = new THREE.Texture( canvas );

	mesh = new THREE.Mesh( new THREE.PlaneGeometry( screenSize, screenSize ), material );
	scene.add( mesh );

	renderer = new THREE.WebGLRenderer( { antialias: true } );
	renderer.setPixelRatio( window.devicePixelRatio );
	renderer.setSize(window.innerWidth, window.innerHeight);
	renderer.vr.enabled = true;

	document.body.appendChild( renderer.domElement );

	window.addEventListener( 'resize', onWindowResize, false );

	document.body.appendChild( WEBVR.createButton( renderer ) );

	webvrAnimate();
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
	scene.add( mesh );
}

function onWindowResize() {
	camera.aspect = window.innerWidth / window.innerHeight;
	camera.updateProjectionMatrix();

	renderer.setSize( window.innerWidth, window.innerHeight );
}

function webvrAnimate() {
	requestAnimationFrame(webvrAnimate);
	if (renderer) renderer.render( scene, camera );
}
