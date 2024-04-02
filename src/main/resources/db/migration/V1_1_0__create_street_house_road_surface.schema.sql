CREATE SEQUENCE street_id_sequence;
CREATE SEQUENCE house_id_sequence;
CREATE SEQUENCE road_surface_id_sequence;

CREATE TABLE street (
    id INT PRIMARY KEY DEFAULT nextval('street_id_sequence'),
    name VARCHAR(255),
    postal_code BIGINT UNIQUE
);

CREATE TABLE house (
    id INT PRIMARY KEY DEFAULT nextval('house_id_sequence'),
    house_number VARCHAR(255),
    build_date DATE,
    num_floors INT,
    type VARCHAR(255),
    street_id INT,
    FOREIGN KEY (street_id) REFERENCES street(id),
    UNIQUE (house_number, street_id)
);

CREATE TABLE road_surface (
    id INT PRIMARY KEY DEFAULT nextval('road_surface_id_sequence'),
    type VARCHAR(255) UNIQUE,
    description VARCHAR(255),
    friction_coefficient DOUBLE PRECISION
);

CREATE TABLE road_surface_street (
    road_surface_id INT,
    street_id INT,
    PRIMARY KEY (road_surface_id, street_id),
    FOREIGN KEY (road_surface_id) REFERENCES road_surface(id),
    FOREIGN KEY (street_id) REFERENCES street(id)
);
