var svgns = "http://www.w3.org/2000/svg";
for( var x=0; x < 5000; x += 50 ){
  for( var y=0; y < 3000; y += 50 ){

    var rect = document.createElementNS( svgns,'rect' );
    rect.setAttributeNS( null,'x',x );
    rect.setAttributeNS( null,'y',y );
    rect.setAttributeNS( null,'width','50' );
    rect.setAttributeNS( null,'height','50' );
    rect.setAttributeNS( null,'fill','#'+Math.round( 0xffffff * Math.random()).toString(16) );
    document.getElementById( 'svgOne' ).appendChild( rect );
  }
}