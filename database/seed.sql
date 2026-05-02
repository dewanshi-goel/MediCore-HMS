INSERT INTO users (username,password_hash,role) VALUES
('admin','admin123','admin'),
('doctor1','doc123','doctor'),
('patient1','pat123','patient');

INSERT INTO doctors VALUES
('D001',2,'Dr. Kavya Nair','Cardiology','9988776655','Mon,Wed,Fri');

INSERT INTO patients VALUES
('P001',3,'Riya Sharma','1995-03-12','Female','B+','9876543210','Mysuru','D001');
