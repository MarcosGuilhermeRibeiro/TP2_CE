
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author marcos
 */

/*
    Dúvidas:
    * Qual a aplicação do F
    * E se tiver X fora do domínio?
        Exemplo ind1 = 10, ind2 = 0, ind3 = 10.    Domínio:[0, 10]
        
        F*(ind1 - ind2) + ind3
        0.5*(10 - 0) + 10
            5 + 10 = 15

    
    * Como existe apenas um X que satisfaz as duas condições, otimizar
      significa encontrar esse X?
    * Como faz a punição no indivíduo?
    * Os x's que satisfazem as restrições G(x) e H(x) são na verdade:
      (-1/3) + N onde N é um numero inteiro, positivou ou negatico,
        o que equivale a uma volta positiva ou negativa no circulo trigonométrico.
    * OBS: como definir o coeficiente de punição? pois só consegui encontrar
      o valor ideal aumentando empiricamente o coeficiente de punição.


*/
public class DiferencialEvolution {

    static final int N = 1;
    static final int AMOUNT_INDIVIDUALS = 10;
    static final double F = 0.5;

    static Individual population[];

    public static void main(String[] args) {
        population = new Individual[AMOUNT_INDIVIDUALS];

        generate_population();
        calculate_fitness_population();

        for (int i = 0; i < 10000; i++) {
            generate_new_population(population);
            Individual betterIndividual = get_better_individual(population);
            double media = get_avg_fitness_population(population);
            
            System.out.println("Fitness=" + betterIndividual.getFitness()+ " img Média individuos: " + media);
            System.out.println("Vetor X: ");
            for (int j = 0; j < N; j++) {
                System.out.print(betterIndividual.getX()[j]+"; ");
            }
            System.out.println("");
            
//            melhorFitness.add(i, betterIndividual.getFitness());
//            mediaFitness.add(i, media);
        }

    }

    public static void generate_population() {
        double number, x;
        for (int i = 0; i < population.length; i++) {
            Individual ind = new Individual(N);
            for (int j = 0; j < N; j++) {
                number = Math.random();
                x = number * (2 * 5.12) - 5.12;
                //Domínio: − 5,12 ≤ 𝑥i ≤ 5,12
                ind.getX()[j] = x;

            }
            population[i] = ind;
        }
    }

    public static void calculate_fitness_population() {

        for (int i = 0; i < population.length; i++) {
            calculate_fitness_individual(population[i]);
        }
    }

    public static void calculate_fitness_individual(Individual individual) {

        double fitness;

        //F(X) = 10*N + Somatorio(Xi^2 - 10cos(2*pi*xi)
        //               i=1 ate N
        // sujeito a G(x) = sen(2𝜋𝑥i) + 0,5 ≤ 0    i = 1,2, . . , n
        //           H(x) = cos(2𝜋𝑥j) + 0,5 = 0    j = 1,2, . . , n
        double fx = 10 * N, gx;

        double somafx = 0, somap_gx = 0, somap_hx = 0, hx;
        for (int j = 0; j < N; j++) {
            double x = individual.getX()[j];
            somafx = somafx + x * x - 10 * Math.cos(2 * Math.PI * x);

            gx = Math.sin(2 * Math.PI * x) + 0.5;
            if (gx > 0) {
                somap_gx += gx;
            }

            hx = Math.cos(2 * Math.PI * x) + 0.5;
            somap_hx += Math.abs(hx);
        }

        fx = fx + somafx;

        fitness = fx + 1000*somap_hx + 100*somap_gx;//fx + somap_gx + somap_hx;

        individual.setFitness(fitness);

    }

    public static void generate_new_population(Individual population[]) {
        Individual vector_noise, ind_experiment;
        for (int i = 0; i < population.length; i++) {
            vector_noise = diference_and_mutation(population, i);
            ind_experiment = cross_breeding(population[i], vector_noise);

            calculate_fitness_individual(ind_experiment);

            select_better_individual(population, i, ind_experiment);
        }
    }

    public static Individual diference_and_mutation(Individual population[], int i) {
        Individual ind1, ind2, ind3, ind_diference = new Individual(N), ind_noise = new Individual(N);

        int numbers[] = random_numbers_not_repeat(0, AMOUNT_INDIVIDUALS - 1, 3);

        while (numbers[0] == i || numbers[1] == i || numbers[2] == i) {
            numbers = random_numbers_not_repeat(0, AMOUNT_INDIVIDUALS - 1, 3);
        }

        ind1 = population[numbers[0]];
        ind2 = population[numbers[1]];
        ind3 = population[numbers[2]];

        for (int j = 0; j < N; j++) {
            ind_diference.getX()[j] = F * (ind1.getX()[j] - ind2.getX()[j]);
            ind_noise.getX()[j] = ind_diference.getX()[j] + ind3.getX()[j];
        }

        return ind_noise;
    }

    static int[] random_numbers_not_repeat(int a, int b, int amount_numbers) {
        int size_vector = amount_numbers;

        List<Integer> numbers = new ArrayList<>();
        for (int i = a; i <= b; i++) {

            numbers.add(i);
        }

        Collections.shuffle(numbers);

        int vector_return[] = new int[size_vector];
        for (int i = 0; i < size_vector; i++) {
            vector_return[i] = numbers.get(i);
        }

        return vector_return;
    }

    public static Individual cross_breeding(Individual ind_i, Individual ind_noise) {
        Individual ind_experiment = new Individual(N);
        int cont_noise = 0;
        
        for (int i = 0; i < N; i++) {

            double number = Math.random();

            if (number <= 0.8) {
                
                ind_experiment.getX()[i] = ind_i.getX()[i];
            } else {
                
                ind_experiment.getX()[i] = ind_noise.getX()[i];
                cont_noise++;
            }
        }
        if (cont_noise == 0){
            int number[] = random_numbers_not_repeat(0, N-1, 1);
            
            ind_experiment.getX()[number[0]] = ind_noise.getX()[number[0]];
        }
        
        return ind_experiment;
    }

    private static void select_better_individual(Individual[] population, int i, Individual ind_experiment) {

        if (ind_experiment.getFitness() < population[i].getFitness()) {
            population[i] = ind_experiment;
        }
    }
    
    public static Individual get_better_individual(Individual population[]) {
        double menorFitness = population[0].getFitness();
        Individual betterIndividual = population[0];

        for (int i = 1; i < population.length; i++) {
            Individual individual = population[i];

            if (individual.getFitness() < menorFitness) {
                menorFitness = individual.getFitness();
                betterIndividual = population[i];
            }
        }

        return betterIndividual;
    }
    
    public static double get_avg_fitness_population(Individual[] population) {
        double soma = 0;
        for (int i = 0; i < population.length; i++) {
            Individual individual = population[i];
            soma += individual.getFitness();
        }
        double media = soma / AMOUNT_INDIVIDUALS;
        return media;
    }
}